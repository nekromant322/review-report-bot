package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.MessageContants.REPORT_REMINDER;

@Component
public class ReminderScheduler {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    private MentoringReviewBot mentoringReviewBot;

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

}
