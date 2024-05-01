package com.nekromant.telegram.service;

import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentDetailsService {
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;

    public void save(PaymentDetails paymentDetails){
        log.info(paymentDetails.toString());
        paymentDetailsRepository.save(paymentDetails);
    }

    public PaymentDetails findByNumber(String number) {
        return paymentDetailsRepository.findByNumber(number);
    }
}
