package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Contract;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ContractRepository extends CrudRepository<Contract, String> {
    Optional<Contract> findContractByStudentInfo_UserName(String username);
    Optional<Contract> findContractByStudentInfo_chatId(Long chatId);
}
