package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientPaymentRequestServiceProvider {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    private MentoringSubscriptionRequestService mentoringSubscriptionRequestService;

    public ClientPaymentRequestService getClientPaymentRequestService(ServiceType serviceType) {
        switch (serviceType) {
            case RESUME:
                return resumeAnalysisRequestService;
            case MENTORING:
                return mentoringSubscriptionRequestService;
        }
        return null;
    }
}
