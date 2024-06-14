package com.nekromant.telegram.service;

import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.config.LifePayProperties;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.MentoringSubscriptionRequest;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.repository.MentoringSubscriptionRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.nekromant.telegram.contants.MessageContants.MENTORING_OFFER_DESCRIPTION;
import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_MENTORING_SUBSCRIPTION;

@Slf4j
@Service
public class MentoringSubscriptionRequestService extends ClientPaymentRequestServiceCommon implements ClientPaymentRequestService {
    @Autowired
    private MentoringSubscriptionRequestRepository mentoringSubscriptionRequestRepository;
    @Autowired
    private LifePayProperties lifePayProperties;
    @Autowired
    private PriceProperties priceProperties;

    public ResponseEntity save(Map mentoringData) {
        MentoringSubscriptionRequest mentoringSubscriptionRequest = MentoringSubscriptionRequest.builder()
                .tgName(mentoringData.get("TG-NAME").toString())
                .customerPhone(mentoringData.get("PHONE").toString())
                .build();

        ChequeDTO chequeDTO = new ChequeDTO(lifePayProperties.getLogin(),
                lifePayProperties.getApikey(),
                calculatePriceWithOptionalDiscount(priceProperties.getMentoringSubscription(), null),
                MENTORING_OFFER_DESCRIPTION,
                mentoringData.get("PHONE").toString(),
                lifePayProperties.getMethod());

        return save(ServiceType.MENTORING, chequeDTO, mentoringSubscriptionRequest, mentoringSubscriptionRequestRepository, null);
    }

    public void notifyMentor(PaymentDetails paymentDetails) {
        String text = generateTextForMentoringBotNotification(paymentDetails,
                RESPONSE_FOR_MENTORING_SUBSCRIPTION,
                mentoringSubscriptionRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
        notifyMentor(paymentDetails, text);
    }

    public ServiceType getType(){
        return ServiceType.MENTORING;
    }
}
