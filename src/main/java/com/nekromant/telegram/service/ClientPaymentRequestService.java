package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;

public interface ClientPaymentRequestService {

    void notifyMentor(PaymentDetails paymentDetails);

    void rejectApplication(PaymentDetails paymentDetails);

    ServiceType getType();
}
