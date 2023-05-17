package com.nekromant.telegram.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class PageController {

    @GetMapping("/incomingReviewWidget")
    public String incomingReviewWidget(@RequestParam(value = "mentor") String mentor, Model model) {
        model.addAttribute("mentor", mentor);
        return "incomingReviewWidget.html";
    }
}
