package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.commands.feign.TelegramFeign;
import com.nekromant.telegram.config.CVAnalProp;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import feign.form.FormData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;


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
    private CVAnalProp props;


    public void save(byte[] bytes, String tgName, String phone) {
        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
        resumeAnalysisRequest.setCVPdf(bytes);
        resumeAnalysisRequest.setTgName(tgName);
        resumeAnalysisRequest.setPhone(phone);

        String description = "Оплата за разбор резюме по договору публичной оферты";
        ChequeDTO chequeDTO = new ChequeDTO(props.getLogin(), props.getApikey(), props.getAmount(), description, phone, props.getMethod());

        log.info("Sending request to LifePay" + chequeDTO);
        LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
        log.info("LifePay response: " + lifePayResponse);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setNumber(lifePayResponse.getData().getNumber());
        paymentDetails.setStatus(PayStatus.UNREDEEMED.get());
        paymentDetailsRepository.save(paymentDetails);
        log.info("Unredeemed payment created: " + paymentDetails);

        resumeAnalysisRequest.setLifePayNumber(lifePayResponse.getData().getNumber());
        resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
        log.info("New resume analysis request created: " + resumeAnalysisRequest);
    }

    public void sendCVToMentorForAnalysisOrReject(PaymentDetails paymentDetails) {
        paymentDetailsRepository.deleteByNumber(paymentDetails.getNumber());
        paymentDetailsRepository.save(paymentDetails);

        if (paymentDetails.getStatus().equals(PayStatus.FAIL.get())) {
            log.info("Payment failed: " + paymentDetails);
            resumeAnalysisRequestRepository.deleteByLifePayNumber(paymentDetails.getNumber());
            return;
        }

        log.info("Payment details have been redeemed:" + paymentDetails);

        byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getCVPdf();
        FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
        String receiverId = userInfoService.getUserInfo(props.getReceiverName()).getChatId().toString();
        telegramFeign.sendDocument(formData, receiverId);

        String text = "Зарегистрирован и оплачен заказ на разбор резюме " +
                paymentDetails.getNumber() +
                "\n телефон: " + paymentDetails.getPhone() +
                "\n Telegram nickname: " + resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getTgName();
        mentoringReviewBot.sendMessage(receiverId, text);
        log.info(text);
    }
}
