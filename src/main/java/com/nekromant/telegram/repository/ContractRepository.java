package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Contract;
import org.springframework.data.repository.CrudRepository;

public interface ContractRepository extends CrudRepository<Contract, String> {
}
