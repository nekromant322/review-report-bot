package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.MentoringSubscriptionRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentoringSubscriptionRequestRepository extends CrudRepository<MentoringSubscriptionRequest, Long> {
    MentoringSubscriptionRequest findByLifePayTransactionNumber(String number);
}
