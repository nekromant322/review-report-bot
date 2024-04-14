package com.nekromant.telegram.service;

import com.nekromant.telegram.model.NewClient;
import com.nekromant.telegram.repository.NewClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewClientService {
    @Autowired
    private NewClientRepository newClientRepository;

    public void saveNewBlob(NewClient newClient) {
        newClientRepository.save(newClient);
    }

    public NewClient findNewClientById(long id) {
        return newClientRepository.findById(id).get();
    }

}
