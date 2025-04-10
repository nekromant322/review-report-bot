package com.nekromant.telegram.controller;

import com.nekromant.telegram.config.EnableUtmTracking;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableUtmTracking
public class PricingController {

    @GetMapping
    public String redirectToPricing() {
        return "redirect:/pricing";
    }

    @GetMapping("/pricing")
    public String getPricingPage() {
        return "pricing";
    }

    @GetMapping("/pricingv1")
    public String getResumeAnalysisRequestPage() {
        return "pricing_old";
    }
}
