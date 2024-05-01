package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.commands.feign.TelegramFeign;
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
    @Value("${pay-info.login}")
    private String login;
    @Value("${pay-info.apikey}")
    private String apikey;
    @Value("${pay-info.method}")
    private String method;
    @Value("${pay-info.amount}")
    private String amount;
    @Value("${owner.userName}")
    private String receiverName;

    public void save(byte[] bytes, String tgName, String phone) {
        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
        resumeAnalysisRequest.setCVPdf(bytes);
        resumeAnalysisRequest.setTgName(tgName);
        resumeAnalysisRequest.setPhone(phone);

        String description = "Оплата за разбор резюме по договору публичной оферты";
        ChequeDTO chequeDTO = new ChequeDTO(login, apikey, amount, description, phone, method);

        log.info("Sending request to LifePay" + chequeDTO);
        LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
        log.info("LifePay response: " + lifePayResponse);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setNumber(lifePayResponse.getData().getNumber());
        paymentDetails.setStatus("unredeemed");
        paymentDetailsRepository.save(paymentDetails);
        log.info("Unredeemed payment created: " + paymentDetails);

        resumeAnalysisRequest.setLifePayNumber(lifePayResponse.getData().getNumber());
        resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
        log.info("New resume analysis request created: " + resumeAnalysisRequest);
    }

    public void sendCVToMentorForAnalysis(PaymentDetails paymentDetails) {
        paymentDetailsRepository.deleteByNumber(paymentDetails.getNumber());
        paymentDetailsRepository.save(paymentDetails);
        log.info("Payment details have been redeemed:" + paymentDetails);

        byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getCVPdf();
        FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
        String receiverId = userInfoService.getUserInfo(receiverName).getChatId().toString();
        telegramFeign.sendDocument(formData, receiverId);

        String text = "Зарегистрирован и оплачен заказ на разбор резюме " +
                paymentDetails.getNumber() +
                "\n телефон: " + paymentDetails.getPhone() +
                "\n Telegram nickname: " + resumeAnalysisRequestRepository.findByLifePayNumber(paymentDetails.getNumber()).getTgName();
        mentoringReviewBot.sendMessage(receiverId, text);
        log.info(text);
    }
}
