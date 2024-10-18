package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nekromant.telegram.contants.MessageContants.*;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Component
public class ReportReminderScheduler {

    @Value("${reminders.maxDaysWithoutReport}")
    private Integer maxDaysWithoutReport;
    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private SpecialChatService specialChatService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SendMessageService sendMessageService;

    @Scheduled(cron = "0 0 19 * * *")
    public void everyOneRemindAboutReports() {
        log.info("Процессинг напоминаний об отчетах");
        Set<String> allStudents = reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .filter(this::isNotOwnerOrMentor)
                .collect(Collectors.toSet());

        Set<String> alreadyWroteReportToday = reportRepository.findAllByDateIs(LocalDate.now(ZoneId.of("Europe/Moscow")))
                .stream()
                .map(Report::getStudentUserName)
                .collect(Collectors.toSet());

        allStudents.removeAll(alreadyWroteReportToday);

        if (!allStudents.isEmpty()) {
            sendMessageService.sendMessage(specialChatService.getReportsChatId(), REPORT_REMINDER +
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
                .filter(this::isNotOwnerOrMentor)
                .collect(Collectors.toSet());

        List<String> badStudentsUsernames = new ArrayList<>();
        for (String username : allStudentsUsernames) {


            List<LocalDate> hotestReportsDates = reportRepository.findAllByStudentUserNameIgnoreCase(username).stream()
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
            sendMessageService.sendMessage(specialChatService.getMentorsChatId(),
                    String.format(MENTORS_REMINDER_STUDENT_WITHOUT_REPORTS, maxDaysWithoutReport) +
                            badStudentsUsernames.stream().map(studentName -> "@" + studentName).collect(Collectors.joining(",\n")));
        }

        badStudentsUsernames.stream().
                map(name -> userInfoService.getUserInfo(name).getChatId())
                .forEach(chatId -> sendMessageService.sendMessage(chatId.toString(), STUDENT_REPORT_FORGET_REMINDER));

    }

    private boolean isNotOwnerOrMentor(String username) {
        return !username.equalsIgnoreCase(ownerUserName)
                && !userInfoService.getUserInfo(ownerUserName).getUserType().equals(UserType.MENTOR);
    }

}
