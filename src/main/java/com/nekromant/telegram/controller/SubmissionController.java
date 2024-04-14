package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.processing.Generated;

@Controller
public class SubmissionController {
    @GetMapping("/submit")
    public String getSubmissionPage() {
        return "submission/submission.html";
    }
}
