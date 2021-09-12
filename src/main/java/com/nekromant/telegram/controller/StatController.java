package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/stat")
public class StatController {

    @GetMapping
    public String getStatPage() {
        return "stat";
    }
}
