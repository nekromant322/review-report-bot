package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.MentoringPayRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MentoringPayRepository extends CrudRepository<MentoringPayRequest, Long> {
    MentoringPayRequest findByLifePayTransactionNumber(String number);
}