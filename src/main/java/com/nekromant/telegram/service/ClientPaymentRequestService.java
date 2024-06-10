package com.nekromant.telegram.service;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.feign.TelegramFeign;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.repository.MentoringSubscriptionRequestRepository;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import feign.form.FormData;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.MediaType;

import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_MENTORING_SUBSCRIPTION;
import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_RESUME_PROJARKA;

@Slf4j
@Service
public class ClientPaymentRequestService {
    @Autowired
    private ResumeAnalysisRequestRepository resumeAnalysisRequestRepository;
    @Autowired
    private MentoringSubscriptionRequestRepository mentoringSubscriptionRequestRepository;
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private PriceProperties priceProperties;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private UserInfoService userInfoService;
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;
    @Autowired
    private TelegramFeign telegramFeign;

    private Logger logger;

    public String calculatePriceWithOptionalDiscount(String CVPromocodeId, String clientPaymentRequestService) {
        String basePrice = "";
        switch (clientPaymentRequestService) {
            case "ResumeAnalysisRequestService":
                basePrice = priceProperties.getResumeReview();
                break;
            case "MentoringSubscriptionRequest":
                basePrice = priceProperties.getMentoringSubscription();
                break;
        }

        if (CVPromocodeId.equals("null")) return basePrice;
        return String.valueOf(Math.round(Double.parseDouble(basePrice) * (1 - promocodeService.findById(CVPromocodeId).getDiscountPercent() / 100)));
    }

    @Transactional
    public void notifyMentor(PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), "Payment details have been redeemed:" + paymentDetails);

        String receiverId = userInfoService.getUserInfo(ownerUserName).getChatId().toString();
        String text = null;
        switch (paymentDetails.getServiceType()) {
            case RESUME:
                promocodeService.incrementCounterUsed(paymentDetails);
                byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getCVPdf();
                FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
                telegramFeign.sendDocument(formData, receiverId);
                text = generateTextForMentoringBotNotification(paymentDetails, RESPONSE_FOR_RESUME_PROJARKA, resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
                break;
            case MENTORING:
                text = generateTextForMentoringBotNotification(paymentDetails, RESPONSE_FOR_MENTORING_SUBSCRIPTION, mentoringSubscriptionRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
                break;
        }
        mentoringReviewBot.sendMessage(receiverId, text);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), text);
    }

    public void rejectApplication(PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), "Payment failed: " + paymentDetails);
    }


    public void logInfoByRequestServiceType(ServiceType serviceType, String logMessage) {
        switch (serviceType) {
            case RESUME:
                logger = LoggerFactory.getLogger(ResumeAnalysisRequestService.class);
                break;
            case MENTORING:
                logger = LoggerFactory.getLogger(MentoringSubscriptionRequestService.class);
                break;
        }
        logger.info(logMessage);
    }

    public String generateTextForMentoringBotNotification(PaymentDetails paymentDetails, String response, String tgName) {
        return String.format(response,
                paymentDetails.getNumber(),
                paymentDetails.getPhone(),
                tgName);
    }
}
