package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClientPaymentRequestServiceProvider {

    private Map<ServiceType, ClientPaymentRequestService> map = new HashMap<>();

    @Autowired
    public ClientPaymentRequestServiceProvider(List<ClientPaymentRequestService> list) {
        list.forEach(service -> map.put(service.getType(), service));
    }

    public ClientPaymentRequestService getClientPaymentRequestService(ServiceType serviceType) {
        return map.get(serviceType);
    }
}
