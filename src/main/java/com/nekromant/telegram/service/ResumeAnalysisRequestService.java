package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.commands.feign.TelegramFeign;
import com.nekromant.telegram.config.PricingProperties;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import feign.form.FormData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;

import static java.rmi.server.LogStream.log;


@Slf4j
@Service
public class ResumeAnalysisRequestService {
    @Autowired
    private ResumeAnalysisRequestRepository resumeAnalysisRequestRepository;
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;
    @Autowired
    private LifePayFeign lifePayFeign;
    @Autowired
    private TelegramFeign telegramFeign;

    @Autowired
    @Qualifier("PayInfoProperties")
    private PricingProperties payInfoProperties;

    @Autowired
    @Qualifier("PriceProperties")
    private PricingProperties priceProperties;

    @Autowired
    @Qualifier("OwnerProperties")
    private PricingProperties ownerProperties;


    public ResponseEntity save(byte[] CVPdf, String tgName, String phone) {
        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
        resumeAnalysisRequest.setCVPdf(CVPdf);
        resumeAnalysisRequest.setTgName(tgName);
        resumeAnalysisRequest.setPhone(phone);
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK);

        String description = "Оплата за разбор резюме по договору публичной оферты";
        ChequeDTO chequeDTO = new ChequeDTO(payInfoProperties.getLogin(), payInfoProperties.getApikey(), priceProperties.getResumeReview(), description, phone, payInfoProperties.getMethod());

        try {
            ResumeAnalysisRequestService.log.info("Sending request to LifePay" + chequeDTO);
            LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
            ResumeAnalysisRequestService.log.info("LifePay response: " + lifePayResponse);
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setNumber(lifePayResponse.getData().getNumber());
            paymentDetails.setStatus(PayStatus.UNREDEEMED.get());
            paymentDetailsRepository.save(paymentDetails);
            ResumeAnalysisRequestService.log.info("Unredeemed payment created: " + paymentDetails);

            resumeAnalysisRequest.setLifePayNumber(lifePayResponse.getData().getNumber());
            resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
            ResumeAnalysisRequestService.log.info("New resume analysis request created: " + resumeAnalysisRequest);
        } catch (JsonParseException jsonParseException) {
            log(jsonParseException.getMessage());
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (DataAccessException dataAccessException) {
            log(dataAccessException.getMessage());
            responseEntity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    public void sendCVToMentorForAnalysis(PaymentDetails paymentDetails) {
        paymentDetailsRepository.save(paymentDetails);
        ResumeAnalysisRequestService.log.info("Payment details have been redeemed:" + paymentDetails);

        byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getCVPdf();
        FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
        String receiverId = userInfoService.getUserInfo(ownerProperties.getUserName()).getChatId().toString();
        telegramFeign.sendDocument(formData, receiverId);

        final String RESPONSE_FOR_RESUME_PROJARKA = "Зарегистрирован и оплачен заказ %s на разбор резюме \n телефон: %s \n Telegram nickname: %s";
        String text = String.format(RESPONSE_FOR_RESUME_PROJARKA,
                paymentDetails.getNumber(),
                paymentDetails.getPhone(),
                resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getTgName());

        mentoringReviewBot.sendMessage(receiverId, text);
        ResumeAnalysisRequestService.log.info(text);
    }

    public void RejectApplication(PaymentDetails paymentDetails) {
        paymentDetailsRepository.save(paymentDetails);
        ResumeAnalysisRequestService.log.info("Payment failed: " + paymentDetails);
    }
}
