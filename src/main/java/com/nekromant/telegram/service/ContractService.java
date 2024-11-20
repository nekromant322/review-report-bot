package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Contract;
import com.nekromant.telegram.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.InstanceNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    public List<Contract> getAllContracts() {
        return (List<Contract>) contractRepository.findAll();
    }

    public Contract getContractByUsername(String username) throws InstanceNotFoundException {
        return contractRepository.findContractByStudentInfo_UserName(username).orElseThrow(() -> new InstanceNotFoundException("No contract bound to this username"));
    }

    public Contract getContractByUserId(Long chatId) throws InstanceNotFoundException {
        return contractRepository.findContractByStudentInfo_chatId(chatId).orElseThrow(() -> new InstanceNotFoundException("No contract bound to this user"));
    }

    @Transactional
    public void updateContractByUsername(String username, String contractId, LocalDate date) throws InstanceNotFoundException {
        Contract contract = getContractByUsername(username);
        contract.setContractId(contractId);
        contract.setDate(date);
        saveContract(contract);
    }

    public void saveContract(Contract contract) {
        contractRepository.save(contract);
    }
}
