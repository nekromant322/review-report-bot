package com.nekromant.telegram.service;

import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentDetailsService {
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;

    public void save(PaymentDetails paymentDetails){
        paymentDetailsRepository.save(paymentDetails);
    }
}
