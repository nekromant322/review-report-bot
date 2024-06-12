package com.nekromant.telegram.service;

import com.nekromant.telegram.model.PaymentDetails;

public interface ClientPaymentRequestService {
    public void notifyMentor(PaymentDetails paymentDetails);

    public void rejectApplication(PaymentDetails paymentDetails);
}
