package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private ChatMessageService chatMessageService;

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteByUserName(String userName) {
        UserInfo userInfo = userInfoService.getUserInfo(userName);
        reportRepository.findAllByUserInfo(userInfo).forEach(report -> {
            chatMessageService.deleteByReport(report);
            reportRepository.delete(report);
        });
    }

    public Report getTemporaryReport(Message message) {
        String[] strings = message.getText().split(" ");
        strings = Arrays.copyOfRange(strings, 1, strings.length);

        return getTemporaryReport(strings, message.getFrom());
    }

    public Report getTemporaryReport(String[] strings, User user) {
        Report report = new Report();

        report.setHours(parseHours(strings));
        report.setTitle(parseTitle(strings));
        report.setUserInfo(userInfoService.getUserInfo(user.getId()));
        return report;
    }

    public void updateReportFromEditedMessage(String editedText, Report report) {
        String[] strings = editedText.split(" ");
        strings = Arrays.copyOfRange(strings, 1, strings.length);

        report.setHours(parseHours(strings));
        report.setTitle(parseTitle(strings));
    }

    private int parseHours(String[] strings) {
        int newHours = Integer.parseInt(strings[0]);
        validateHoursArgument(newHours);
        return newHours;
    }

    private String parseTitle(String[] strings) {
        return Arrays.stream(strings).skip(1).collect(Collectors.joining(" "));
    }

    private void validateHoursArgument(int hours) {
        if (hours < 0 || hours > 24) {
            throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 24");
        }
    }

    public UserStatistic getUserStats(Long chatId) {
        Integer studyDays = reportRepository.findTotalStudyDaysByUserInfo_ChatId(chatId);
        Integer totalHours = reportRepository.findTotalHoursByUserInfo_ChatId(chatId);
        Report firstReport =
                reportRepository.findAllByUserInfo_ChatId(chatId).stream()
                        .filter(this::hasRequiredFields)
                        .min(Comparator.comparing(Report::getDate))
                        .orElse(Report.builder()
                                .date(LocalDate.now())
                                .build()
                        );
        long totalDays = DAYS.between(firstReport.getDate(), LocalDate.now(ZoneId.of("Europe/Moscow")));

        return UserStatistic.builder()
                .userName(userInfoService.getUserInfo(chatId).getUserName())
                .totalDays((int) totalDays)
                .studyDays(studyDays)
                .totalHours(totalHours)
                .averagePerWeek((float) totalHours * 7.f / totalDays)
                .build();
    }

    public List<UserStatistic> getAllUsersStats() {
        return reportRepository.findAll()
                .stream()
                .map(Report::getUserInfo)
                .filter(Objects::nonNull)
                .map(UserInfo::getChatId)
                .distinct()
                .map(this::getUserStats)
                .collect(Collectors.toList());
    }

    private boolean hasRequiredFields(Report report) {
        return report.getDate() != null && report.getHours() != null && report.getTitle() != null;
    }
}
