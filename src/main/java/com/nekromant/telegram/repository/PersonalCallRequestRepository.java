package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.PersonalCallRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalCallRequestRepository extends CrudRepository<PersonalCallRequest, Long> {
    PersonalCallRequest findByLifePayTransactionNumber(String number);
}
