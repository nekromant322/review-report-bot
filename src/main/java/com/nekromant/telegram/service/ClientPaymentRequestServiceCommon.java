package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
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
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public ResponseEntity save(ServiceType serviceType, ChequeDTO chequeDTO, ClientPaymentRequest paymentRequest, CrudRepository repository, String promocodeId) {

        try {
            logger.info("Sending request to LifePay: {}", chequeDTO);
            LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
            logger.info("Life pay response: {}", lifePayResponse);

            PaymentDetails paymentDetails = PaymentDetails.builder()
                    .number(lifePayResponse.getData().getNumber())
                    .status(PayStatus.UNREDEEMED)
                    .serviceType(serviceType)
                    .build();
            paymentDetailsRepository.save(paymentDetails);
            logger.info("Unredeemed payment created: {}", paymentDetails);

            paymentRequest.setLifePayTransactionNumber(lifePayResponse.getData().getNumber());
            repository.save(paymentRequest);
            logger.info("New client payment request created: {}", paymentRequest);

            Promocode promocode = promocodeService.findById(promocodeId);
            if (promocode != null) {
                Set<PaymentDetails> promocodePaymentDetailsSet = promocode.getPaymentDetailsSet();
                promocodePaymentDetailsSet.add(paymentDetails);
                promocode.setPaymentDetailsSet(promocodePaymentDetailsSet);
                promocodeService.save(promocode);
            }
            return ResponseEntity.ok(lifePayResponse.getData().getPaymentUrlWeb());
        } catch (JsonParseException jsonParseException) {
            log("Error while parsing Json: " + jsonParseException.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException dataAccessException) {
            log("Error while accessing database: " + dataAccessException.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    public void notifyMentor(PaymentDetails paymentDetails, String text) {
        promocodeService.incrementCounterUsed(paymentDetails);
        paymentDetailsService.save(paymentDetails);
        logger.info("Payment details have been redeemed: {}", paymentDetails);
        String receiverId = userInfoService.getUserInfo(ownerUserName).getChatId().toString();
        mentoringReviewBot.sendMessage(receiverId, text);
        logger.info(text);
    }

    public void rejectApplication(PaymentDetails paymentDetails) {
        paymentDetailsService.save(paymentDetails);
        logger.info("Payment failed: {}", paymentDetails);
    }

    public String calculatePriceWithOptionalDiscount(String basePrice, String promocodeId) {
        Promocode promocode = promocodeService.findById(promocodeId);
        if (promocode == null) return basePrice;
        return String.valueOf(Math.round(Double.parseDouble(basePrice) * (1 - promocode.getDiscountPercent() / 100)));
    }

    public String generateTextForMentoringBotNotification(PaymentDetails paymentDetails, String response, String tgName) {
        return String.format(response,
                paymentDetails.getNumber(),
                paymentDetails.getPhone(),
                tgName);
    }
}
