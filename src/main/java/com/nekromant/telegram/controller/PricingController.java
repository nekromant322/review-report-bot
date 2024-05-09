package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PricingController {
    @GetMapping("/pricing")
    public String getResumeAnalysisRequestPage() {
        return "pricing";
    }
}
