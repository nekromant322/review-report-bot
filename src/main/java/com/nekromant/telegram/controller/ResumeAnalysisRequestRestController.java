package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ResumeAnalysisRequestRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;

    @PostMapping("/pricing")
    @Modifying
    public ResponseEntity submitNewResumeAnalysisRequest(@RequestParam("form_data") MultipartFile formData,
                                                         @RequestHeader("TG-NAME") String tgName,
                                                         @RequestHeader("PHONE") String phone) throws Exception {
       return resumeAnalysisRequestService.save(formData.getBytes(), tgName, phone);
    }
}
