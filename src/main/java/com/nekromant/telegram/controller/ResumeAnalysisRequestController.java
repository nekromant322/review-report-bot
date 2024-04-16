package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResumeAnalysisRequestController {
    @GetMapping("/resume/submit")
    public String getResumeAnalysisRequestPage() {
        return "submission/submission.html";
    }
}
