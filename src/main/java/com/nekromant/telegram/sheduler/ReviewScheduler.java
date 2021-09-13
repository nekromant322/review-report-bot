package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.MessageContants.REPORT_REMINDER;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_INCOMING;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Component
public class ReviewScheduler {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    private ReportRepository reportRepository;

    @Scheduled(cron = "0 55 * * * *")
    public void processEveryHour() {

//        cleanUp();
        notifyReview();
    }

    @Scheduled(cron = "0 0 19 * * *")
    public void remindAboutReports() {
        System.out.println("Процесинг напоминаний об отчетах");
        Set<String> allStudents = reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .collect(Collectors.toSet());

        Set<String> alreadyWroteReportToday = reportRepository.findAllByDateIs(LocalDate.now(ZoneId.of("Europe/Moscow")))
                .stream()
                .map(Report::getStudentUserName)
                .collect(Collectors.toSet());

        allStudents.removeAll(alreadyWroteReportToday);

        mentoringReviewBot.sendMessage(specialChatService.getReportsChatId(), REPORT_REMINDER +
                allStudents.stream()
                        .map(username -> "@" + username)
                        .collect(Collectors.joining(", ")));
    }

    private void notifyReview() {
        System.out.println("Отправка уведомлений");
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();

        List<ReviewRequest> sudenReviews = reviewRequestRepository
                .findAllByBookedDateTimeBetween(nowInMoscow, nowInMoscow.plus(30, ChronoUnit.MINUTES));

        for (ReviewRequest reviewRequest : sudenReviews) {
            String reviewIncomingMessage = String.format(REVIEW_INCOMING,
                    reviewRequest.getStudentUserName(), reviewRequest.getMentorUserName(),
                    reviewRequest.getBookedDateTime().format(defaultDateTimeFormatter()), reviewRequest.getTitle());
            mentoringReviewBot.sendMessage(reviewRequest.getStudentChatId(), reviewIncomingMessage);
            mentoringReviewBot.sendMessage(specialChatService.getMentorsChatId(), reviewIncomingMessage);
        }
    }

    private void cleanUp() {
        System.out.println("Удаление старых запросов");
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();
        reviewRequestRepository.deleteAllByBookedDateTimeIsBefore(nowInMoscow.minus(1, ChronoUnit.DAYS));
    }
}
