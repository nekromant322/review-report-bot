package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_INCOMING;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Slf4j
@Component
public class ReviewScheduler {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private MentorRepository mentorRepository;

    @Scheduled(cron = "0 55 * * * *")
    public void processEveryHour() {
//        расскоментить, если хочется чтоб история ревью удалялась
//        cleanUp();

        notifyReview();
    }

    private void notifyReview() {
        log.info("Ежечасная отправка уведомлений о назначенных ревью");
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();

        List<ReviewRequest> sudenReviews = reviewRequestRepository
                .findAllByBookedDateTimeBetween(nowInMoscow, nowInMoscow.plus(30, ChronoUnit.MINUTES));

        for (ReviewRequest reviewRequest : sudenReviews) {
            String reviewIncomingMessage = String.format(REVIEW_INCOMING,
                    reviewRequest.getStudentUserName(), reviewRequest.getMentorUserName(),
                    reviewRequest.getBookedDateTime().format(defaultDateTimeFormatter()),
                    reviewRequest.getTitle(),
                    mentorRepository.findMentorByUserNameIgnoreCase(reviewRequest.getMentorUserName()).getRoomUrl());
            userInfoService.getAllUsersReportNotificationsEnabled()
                    .stream()
                    .filter(x -> !x.getUserName().equalsIgnoreCase(reviewRequest.getStudentUserName()))
                    .filter(x -> !x.getUserName().equalsIgnoreCase(reviewRequest.getMentorUserName()))
                    .forEach(x -> mentoringReviewBot.sendMessage(x.getChatId().toString(), reviewIncomingMessage));
            mentoringReviewBot.sendMessage(reviewRequest.getStudentChatId(), reviewIncomingMessage);
            mentoringReviewBot.sendMessage(userInfoService.getUserInfo(reviewRequest.getMentorUserName()).getChatId().toString(),
                    reviewIncomingMessage);
        }
    }

    private void cleanUp() {
        log.info("Удаление старых запросов");
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();
        reviewRequestRepository.deleteAllByBookedDateTimeIsBefore(nowInMoscow.minus(1, ChronoUnit.DAYS));
    }
}
