package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.MentorsChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_INCOMING;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Component
public class ReviewScheduler {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @Autowired
    private MentorsChatService mentorsChatService;

    @Scheduled(cron = "0 55 * * * *")
    public void processEveryHour() {
        cleanUp();
        notifyReview();
    }

    private void notifyReview() {
        System.out.println("Отправка уведомлений");
        List<ReviewRequest> sudenReviews = reviewRequestRepository
                .findAllByBookedDateTimeBetween(LocalDateTime.now(), LocalDateTime.now().plus(30, ChronoUnit.MINUTES));

        for (ReviewRequest reviewRequest : sudenReviews) {
            String reviewIncomingMessage = String.format(REVIEW_INCOMING,
                    reviewRequest.getStudentUserName(), reviewRequest.getMentorUserName(),
                    reviewRequest.getBookedDateTime().format(defaultDateTimeFormatter()), reviewRequest.getTitle());
            mentoringReviewBot.sendMessage(reviewRequest.getStudentChatId(), reviewIncomingMessage);
            mentoringReviewBot.sendMessage(mentorsChatService.getMentorsChatId(), reviewIncomingMessage);
        }
    }

    private void cleanUp() {
        System.out.println("Удаление старых запросов");
        reviewRequestRepository.deleteAllByBookedDateTimeIsBefore(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
    }
}
