package com.nekromant.telegram.service;

import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.config.LifePayProperties;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.PersonalCallRequest;
import com.nekromant.telegram.repository.PersonalCallRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.nekromant.telegram.contants.MessageContants.PERSONAL_CALL_DESCRIPTION;
import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_PERSONAL_CALL;

@Slf4j
@Service
public class PersonalCallRequestService extends ClientPaymentRequestServiceCommon implements ClientPaymentRequestService {
    @Autowired
    private PersonalCallRequestRepository personalCallRequestRepository;
    @Autowired
    private LifePayProperties lifePayProperties;
    @Autowired
    private PriceProperties priceProperties;

    public ResponseEntity save (Map callData) {
        PersonalCallRequest personalCallRequest = PersonalCallRequest.builder()
                .tgName(callData.get("TG-NAME").toString())
                .customerPhone(callData.get("PHONE").toString())
                .build();
        String promocodeId = callData.get("CALL-PROMOCODE-ID") == null ? null : callData.get("CALL-PROMOCODE-ID").toString();
        ChequeDTO chequeDTO = new ChequeDTO(lifePayProperties.getLogin(),
                lifePayProperties.getApikey(),
                calculatePriceWithOptionalDiscount(priceProperties.getPersonalCall(), promocodeId),
                PERSONAL_CALL_DESCRIPTION,
                callData.get("PHONE").toString(),
                lifePayProperties.getMethod());

        return save(ServiceType.CALL, chequeDTO, personalCallRequest, personalCallRequestRepository, promocodeId);
    }

    @Override
    public void notifyMentor(PaymentDetails paymentDetails) {
        String text = generateTextForMentoringBotNotification(paymentDetails,
                RESPONSE_FOR_PERSONAL_CALL,
                personalCallRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
        notifyMentor(paymentDetails, text);
    }

    @Override
    public ServiceType getType() {
        return ServiceType.CALL;
    }
}
