package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nekromant.telegram.contants.MessageContants.MENTORS_REMINDER_STUDENT_WITHOUT_REPORTS;
import static com.nekromant.telegram.contants.MessageContants.REPORT_REMINDER;
import static com.nekromant.telegram.contants.MessageContants.STUDENT_REPORT_FORGET_REMINDER;
import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class ReportReminderScheduler {

    @Value("${reminders.maxDaysWithoutReport}")
    private Integer maxDaysWithoutReport;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @Autowired
    private UserInfoService userInfoService;

    @Scheduled(cron = "0 0 19 * * *")
    public void everyOneRemindAboutReports() {
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

        if (!allStudents.isEmpty()) {
            mentoringReviewBot.sendMessage(specialChatService.getReportsChatId(), REPORT_REMINDER +
                allStudents.stream()
                        .map(username -> "@" + username)
                        .collect(Collectors.joining(", ")));
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void reminderAboutStudentsWithoutReports() {

        Set<String> allStudentsUsernames = reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .collect(Collectors.toSet());

        List<String> badStudentsUsernames = new ArrayList<>();
        for (String username : allStudentsUsernames) {


            List<LocalDate> hotestReportsDates = reportRepository.findAllByStudentUserName(username).stream()
                    .sorted(Comparator.comparing(Report::getDate).reversed())
                    .limit(maxDaysWithoutReport)
                    .map(Report::getDate)
                    .collect(Collectors.toList());


            List<LocalDate> recentDates = Stream.iterate(LocalDate.now(ZoneId.of("Europe/Moscow")),
                    date -> date.minus(1, DAYS))
                    .limit(maxDaysWithoutReport)
                    .collect(Collectors.toList());

            boolean hasRecentReport = false;
            for (LocalDate recentDate : recentDates) {
                if (hotestReportsDates.contains(recentDate)) {
                    hasRecentReport = true;
                }
            }

            if (!hasRecentReport) {
                badStudentsUsernames.add(username);
            }


        }
        if (badStudentsUsernames.size() > 0) {
            mentoringReviewBot.sendMessage(specialChatService.getMentorsChatId(),
                    String.format(MENTORS_REMINDER_STUDENT_WITHOUT_REPORTS, maxDaysWithoutReport) +
                            badStudentsUsernames.stream().map(studentName -> "@" + studentName).collect(Collectors.joining(",\n")));
        }

        badStudentsUsernames.stream().
                map(name -> userInfoService.getUserInfo(name).getChatId())
                .forEach(chatId -> mentoringReviewBot.sendMessage(chatId.toString(), STUDENT_REPORT_FORGET_REMINDER));

    }

}
