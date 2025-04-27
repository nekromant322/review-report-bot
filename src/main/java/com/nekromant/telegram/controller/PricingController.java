package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.UtmTag;
import com.nekromant.telegram.service.UtmTagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class PricingController {
    private final UtmTagsService utmTagsService;

    @GetMapping
    public String redirectToPricing() {
        return "redirect:/pricing";
    }

    @GetMapping("/pricing")
    public String getPricingPage(@RequestParam(required = false) String source,
                                 HttpServletResponse response) {
        if (source != null) {
            UtmTag tag = utmTagsService.getTag(source, UtmTag.PRICE_SECTION);
            response.addCookie(utmTagsService.setCookieByUtmTag(tag));
        }
        return "pricing";
    }

    @GetMapping("/pricingv1")
    public String getResumeAnalysisRequestPage() {
        return "pricing_old";
    }
}
