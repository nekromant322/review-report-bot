package com.nekromant.telegram.service.update_handler.callback_strategy.utils;

import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;

@Component
public class StrategyUtils {
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    public ReviewRequest getReviewRequest(Long reviewId) {
        return reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
    }
}
