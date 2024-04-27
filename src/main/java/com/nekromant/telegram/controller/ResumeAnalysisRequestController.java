package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResumeAnalysisRequestController {
    @GetMapping("/pricing")
    public String getResumeAnalysisRequestPage() {
        return "pricing";
    }
}
