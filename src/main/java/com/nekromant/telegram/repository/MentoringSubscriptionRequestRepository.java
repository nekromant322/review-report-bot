package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.MentoringSubscriptionRequest;
import org.springframework.data.repository.CrudRepository;

public interface MentoringSubscriptionRequestRepository extends CrudRepository<MentoringSubscriptionRequest, Long> {
    MentoringSubscriptionRequest findByLifePayNumber(String number);
}
