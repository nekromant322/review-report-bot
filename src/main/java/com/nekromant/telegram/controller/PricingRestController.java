package com.nekromant.telegram.controller;

import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.service.MentoringSubscriptionRequestService;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/pricing")
public class PricingRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    private MentoringSubscriptionRequestService mentoringSubscriptionRequestService;
    @Autowired
    private PriceProperties priceProperties;

    @PostMapping("/cv")
    @Modifying
    public ResponseEntity submitNewResumeAnalysisRequest(@RequestParam("form_data") MultipartFile formData,
                                                         @RequestHeader("TG-NAME") String tgName,
                                                         @RequestHeader("PHONE") String phone,
                                                         @RequestHeader("CV-PROMOCODE-ID") String CVPromocodeId) throws Exception {
        return resumeAnalysisRequestService.save(formData.getBytes(), tgName, phone, CVPromocodeId);
    }

    @PostMapping("/mentoring")
    public ResponseEntity submitNewMentoringSubscription(@RequestBody Map mentoring_data) {
        return mentoringSubscriptionRequestService.save(mentoring_data);
    }

    @GetMapping("/cv/price")
    public String getCVRoastingPrice() {
        return priceProperties.getResumeReview();
    }

    @GetMapping("/mentoring/price")
    public String getMentoringPrice() {
        return priceProperties.getMentoringSubscription();
    }
}
