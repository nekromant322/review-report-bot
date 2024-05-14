package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.commands.feign.TelegramFeign;
import com.nekromant.telegram.config.LifePayProperties;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import feign.form.FormData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;

import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_RESUME_PROJARKA;
import static com.nekromant.telegram.contants.MessageContants.RESUME_OFFER_DESCRIPTION;
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
    private LifePayProperties lifePayProperties;
    @Autowired
    private PriceProperties priceProperties;
    @Value("${owner.userName}")
    private String ownerUserName;

    public ResponseEntity save(byte[] CVPdf, String tgName, String phone) {
        ResumeAnalysisRequest resumeAnalysisRequest = ResumeAnalysisRequest.builder()
                .CVPdf(CVPdf)
                .tgName(tgName)
                .customerPhone(phone)
                .build();

        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK);

        ChequeDTO chequeDTO = new ChequeDTO(lifePayProperties.getLogin(),
                lifePayProperties.getApikey(),
                priceProperties.getResumeReview(),
                RESUME_OFFER_DESCRIPTION,
                phone,
                lifePayProperties.getMethod());

        try {
            ResumeAnalysisRequestService.log.info("Sending request to LifePay" + chequeDTO);
            LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
            responseEntity = ResponseEntity.status(HttpStatus.OK).body(lifePayResponse.getData().getPaymentUrlWeb());
            ResumeAnalysisRequestService.log.info("LifePay response: " + lifePayResponse);
            PaymentDetails paymentDetails = PaymentDetails.builder()
                    .number(lifePayResponse.getData().getNumber())
                    .status(PayStatus.UNREDEEMED)
                    .serviceType(ServiceType.RESUME)
                    .build();
            paymentDetailsRepository.save(paymentDetails);
            ResumeAnalysisRequestService.log.info("Unredeemed payment created: " + paymentDetails);

            resumeAnalysisRequest.setLifePayTransactionNumber(lifePayResponse.getData().getNumber());
            resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
            ResumeAnalysisRequestService.log.info("New resume analysis request created: " + resumeAnalysisRequest);
        } catch (JsonParseException jsonParseException) {
            log("Erorr while parsing Json: " + jsonParseException.getMessage());
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (DataAccessException dataAccessException) {
            log("Error while accessing database: " + dataAccessException.getMessage());
            responseEntity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    public void sendCVToMentorForAnalysis(PaymentDetails paymentDetails) {
        paymentDetailsRepository.save(paymentDetails);
        ResumeAnalysisRequestService.log.info("Payment details have been redeemed:" + paymentDetails);

        byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getCVPdf();
        FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
        String receiverId = userInfoService.getUserInfo(ownerUserName).getChatId().toString();
        telegramFeign.sendDocument(formData, receiverId);

        String text = String.format(RESPONSE_FOR_RESUME_PROJARKA,
                paymentDetails.getNumber(),
                paymentDetails.getPhone(),
                resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());

        mentoringReviewBot.sendMessage(receiverId, text);
        ResumeAnalysisRequestService.log.info(text);
    }

    public void RejectApplication(PaymentDetails paymentDetails) {
        paymentDetailsRepository.save(paymentDetails);
        ResumeAnalysisRequestService.log.info("Payment failed: " + paymentDetails);
    }
}
