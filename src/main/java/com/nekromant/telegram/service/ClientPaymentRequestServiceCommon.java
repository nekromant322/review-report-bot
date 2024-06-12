package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.ClientPaymentRequest;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.rmi.server.LogStream.log;

@Slf4j
@Service
public class ClientPaymentRequestServiceCommon {

    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private PriceProperties priceProperties;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private LifePayFeign lifePayFeign;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;
    private Logger logger;


    public ResponseEntity save(ServiceType serviceType, ChequeDTO chequeDTO, ClientPaymentRequest paymentRequest, CrudRepository repository, String promocodeId) {

        try {
            logInfoByRequestServiceType(serviceType, "Sending request to LifePay" + chequeDTO);

            LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
            logInfoByRequestServiceType(serviceType, "LifePay response: " + lifePayResponse);

            PaymentDetails paymentDetails = PaymentDetails.builder()
                    .number(lifePayResponse.getData().getNumber())
                    .status(PayStatus.UNREDEEMED)
                    .serviceType(serviceType)
                    .build();
            paymentDetailsRepository.save(paymentDetails);
            logInfoByRequestServiceType(serviceType, "Unredeemed payment created: " + paymentDetails);

            paymentRequest.setLifePayTransactionNumber(lifePayResponse.getData().getNumber());
            repository.save(paymentRequest);
            logInfoByRequestServiceType(serviceType, "New resume analysis request created: " + paymentRequest);

            Promocode promocode = promocodeService.findById(promocodeId);
            if (promocode != null) {
                Set<PaymentDetails> promocodePaymentDetailsSet = promocode.getPaymentDetailsSet();
                promocodePaymentDetailsSet.add(paymentDetails);
                promocode.setPaymentDetailsSet(promocodePaymentDetailsSet);
                promocodeService.save(promocode);
            }
            return ResponseEntity.ok(lifePayResponse.getData().getPaymentUrlWeb());
        } catch (JsonParseException jsonParseException) {
            log("Erorr while parsing Json: " + jsonParseException.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException dataAccessException) {
            log("Error while accessing database: " + dataAccessException.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }


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


    public void notifyMentor(PaymentDetails paymentDetails, String text) {
        paymentDetailsService.save(paymentDetails);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), "Payment details have been redeemed:" + paymentDetails);
        String receiverId = userInfoService.getUserInfo(ownerUserName).getChatId().toString();
        mentoringReviewBot.sendMessage(receiverId, text);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), text);
    }

    public void rejectApplication(PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);
        logInfoByRequestServiceType(paymentDetails.getServiceType(), "Payment failed: " + paymentDetails);
    }


    public void logInfoByRequestServiceType(ServiceType serviceType, String logMessage) {
        Class serviceClass = null;
        switch (serviceType) {
            case RESUME:
                serviceClass = ResumeAnalysisRequestService.class;
                break;
            case MENTORING:
                serviceClass = MentoringSubscriptionRequestService.class;
                break;
        }
        logger = LoggerFactory.getLogger(serviceClass);
        logger.info(logMessage);
    }

    public String generateTextForMentoringBotNotification(PaymentDetails paymentDetails, String response, String tgName) {
        return String.format(response,
                paymentDetails.getNumber(),
                paymentDetails.getPhone(),
                tgName);
    }
}
