package com.nekromant.telegram.service;

import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeAnalysisRequestService {
    @Autowired
    private ResumeAnalysisRequestRepository resumeAnalysisRequestRepository;

    public void saveNewBlob(ResumeAnalysisRequest resumeAnalysisRequest) {
        resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
    }

}
