package com.nekromant.telegram.controller;

import com.google.gson.Gson;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.service.MentoringSubscriptionRequestService;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class ResumeAnalysisRequestRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    MentoringSubscriptionRequestService mentoringSubscriptionRequestService;
    @Autowired
    PriceProperties priceProperties;

    @PostMapping("/pricing/cv")
    @Modifying
    public ResponseEntity submitNewResumeAnalysisRequest(@RequestParam("form_data") MultipartFile formData,
                                                         @RequestHeader("TG-NAME") String tgName,
                                                         @RequestHeader("PHONE") String phone) throws Exception {
        return resumeAnalysisRequestService.save(formData.getBytes(), tgName, phone);
    }

    @PostMapping("/pricing/mentoring")
    public ResponseEntity submitNewMentoringSubscription(@RequestBody Map mentoring_data) {
        return mentoringSubscriptionRequestService.save(mentoring_data);
    }

    @GetMapping("/pricing/cv/price")
    public String getCVRoastingPrice() {
        return priceProperties.getResumeReview();
    }

    @GetMapping("/pricing/mentoring/price")
    public String getMentoringPrice() {
        return priceProperties.getMentoringSubscription();
    }
}
