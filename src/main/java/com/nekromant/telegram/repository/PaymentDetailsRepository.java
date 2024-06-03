package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.PaymentDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDetailsRepository extends CrudRepository<PaymentDetails, Long> {
    PaymentDetails findByNumber(String number);
}
