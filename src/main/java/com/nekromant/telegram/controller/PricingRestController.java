package com.nekromant.telegram.controller;

import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.service.MentoringSubscriptionRequestService;
import com.nekromant.telegram.service.PersonalCallRequestService;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/pricing")
public class PricingRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    private MentoringSubscriptionRequestService mentoringSubscriptionRequestService;
    @Autowired
    private PersonalCallRequestService personalCallRequestService;
    @Autowired
    private PriceProperties priceProperties;

    @PostMapping("/cv")
    @Modifying
    public ResponseEntity submitNewResumeAnalysisRequest(@RequestParam("form_data") MultipartFile formData,
                                                         @RequestHeader("TG-NAME") String tgName,
                                                         @RequestHeader("PHONE") String phone,
                                                         @RequestHeader("CV-PROMOCODE-ID") String CVPromocodeId, HttpServletRequest request) throws Exception {
        tgName = URLDecoder.decode(tgName, StandardCharsets.UTF_8);
        phone = URLDecoder.decode(phone, StandardCharsets.UTF_8);
        CVPromocodeId = URLDecoder.decode(CVPromocodeId, StandardCharsets.UTF_8);
        String prCompany = request.getAttribute("utmDto").toString();
        return resumeAnalysisRequestService.save(formData.getBytes(), tgName, phone, CVPromocodeId, prCompany);
    }

    @PostMapping("/mentoring")
    public ResponseEntity submitNewMentoringSubscription(@RequestBody Map mentoringData, HttpServletRequest request) {
        String prCompany = request.getAttribute("utmDto").toString();
        return mentoringSubscriptionRequestService.save(mentoringData, prCompany);
    }

    @PostMapping("/call")
    public ResponseEntity submitNewCall(@RequestBody Map callData, HttpServletRequest request) {
        String prCompany = request.getAttribute("utmDto").toString();
        return personalCallRequestService.save(callData, prCompany);
    }

    @GetMapping("/cv/price")
    public String getCVRoastingPrice() {
        return priceProperties.getResumeReview();
    }

    @GetMapping("/mentoring/price")
    public String getMentoringPrice() {
        return priceProperties.getMentoringSubscription();
    }

    @GetMapping("/roasting/price")
    public String getRoastingPrice() {
        return priceProperties.getResumeReview();
    }

    @GetMapping("/call/price")
    public String getPersonalCallPrice() {
        return priceProperties.getPersonalCall();
    }
}
