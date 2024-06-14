package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClientPaymentRequestServiceProvider {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    private MentoringSubscriptionRequestService mentoringSubscriptionRequestService;

    private Map<ServiceType, ClientPaymentRequestService> map;

    @PostConstruct
    public void fillMap() {
        map = new HashMap<>();
        map.put(ServiceType.RESUME, resumeAnalysisRequestService);
        map.put(ServiceType.MENTORING, mentoringSubscriptionRequestService);

    }

    public ClientPaymentRequestService getClientPaymentRequestService(ServiceType serviceType) {
        return map.get(serviceType);
    }
}
