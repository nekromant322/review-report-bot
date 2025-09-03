package com.nekromant.telegram.service;

import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.MentoringPayRequest;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.repository.MentoringPayRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_MENTORING;

@Slf4j
@Service
public class MentoringPayService extends ClientPaymentRequestServiceCommon implements ClientPaymentRequestService {
    @Autowired
    private MentoringPayRepository mentoringPayRepository;

    public ResponseEntity save(MentoringPayRequest mentoringPayRequest, ChequeDTO chequeDTO) {
        return save(ServiceType.MENTORING, chequeDTO, mentoringPayRequest, mentoringPayRepository, null, null);
    }

    @Override
    public void notifyMentor(PaymentDetails paymentDetails) {
        String text = generateTextForMentoringBotNotification(paymentDetails,
                RESPONSE_FOR_MENTORING,
                mentoringPayRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
        notifyMentor(paymentDetails, text);
    }

    @Override
    public ServiceType getType() {
        return ServiceType.MENTORING;
    }
}
