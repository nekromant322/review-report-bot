package com.nekromant.telegram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromocodePanelController {
    @GetMapping("/promocode")
    public String getPromocodePanelPage() {
        return "promocode_panel";
    }
}
