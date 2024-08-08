package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PricingControllerV2 {
    @GetMapping("/pricingv2")
    public String getMainPage() {
        return "pricingV2";
    }
}
