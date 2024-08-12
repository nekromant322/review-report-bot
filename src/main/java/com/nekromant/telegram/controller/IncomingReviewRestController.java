package com.nekromant.telegram.controller;

import com.nekromant.telegram.dto.BookedReviewDTO;
import com.nekromant.telegram.service.SchedulePeriodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class IncomingReviewRestController {
    @Autowired
    private SchedulePeriodService schedulePeriodService;

    @GetMapping("/incoming-reviews")
    public List<BookedReviewDTO> getIncomingReviewWithPeriod(@RequestParam(required = false) String mentor) {
        return schedulePeriodService.getBookedReviewList(mentor);
    }
}
