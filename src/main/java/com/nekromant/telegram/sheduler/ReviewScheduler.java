package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_INCOMING;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Slf4j
@Component
public class ReviewScheduler {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private SendMessageService sendMessageService;

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
                    .filter(x -> !x.getUserName().equals(reviewRequest.getStudentUserName()))
                    .filter(x -> !x.getUserName().equals(reviewRequest.getMentorUserName()))
                    .forEach(x -> sendMessageService.sendMessage(x.getChatId().toString(), reviewIncomingMessage));

            sendMessageService.sendMessage(reviewRequest.getStudentChatId(), reviewIncomingMessage);
            sendMessageService.sendMessage(userInfoService.getUserInfo(reviewRequest.getMentorUserName()).getChatId().toString(),
                    reviewIncomingMessage);
        }
    }

    private void cleanUp() {
        log.info("Удаление старых запросов");
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();
        reviewRequestRepository.deleteAllByBookedDateTimeIsBefore(nowInMoscow.minus(1, ChronoUnit.DAYS));
    }
}
