package com.nekromant.telegram.service;

import com.nekromant.telegram.config.PriceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClientPaymentRequestService {
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private PriceProperties priceProperties;

    public String calculatePriceWithOptionalDiscount(String CVPromocodeId, String clientPaymentRequestService) {
        String basePrice = "";
        switch (clientPaymentRequestService) {
            case "ResumeAnalysisRequestService":
                basePrice = priceProperties.getResumeReview();
                break;
            case "MentoringSubscriptionRequest":
                basePrice = priceProperties.getMentoringSubscription();
                break;
        }

        if (CVPromocodeId.equals("null")) return basePrice;
        return String.valueOf(Math.round(Double.parseDouble(basePrice) * (1 - promocodeService.findById(CVPromocodeId).getDiscountPercent() / 100)));
    }

}
