package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.PaymentDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDetailsRepository extends CrudRepository<PaymentDetails, Long> {
    PaymentDetails findByNumber(String number);

    @Query(value = "SELECT * FROM payment_details WHERE created >= :start AND created < :end", nativeQuery = true)
    List<PaymentDetails> findAllByCreatedAtBetween(@Param("start") String start, @Param("end") String end);
}
