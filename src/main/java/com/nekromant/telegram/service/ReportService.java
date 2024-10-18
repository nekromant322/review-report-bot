package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    public Report getTemporaryReport(Message message) {
        String[] strings = message.getText().split(" ");
        strings = Arrays.copyOfRange(strings, 1, strings.length);

        String userName = message.getFrom().getUserName();
        return getTemporaryReport(strings, userName);
    }

    public Report getTemporaryReport(String[] strings, String userName) {
        Report report = new Report();

        report.setHours(parseHours(strings));
        report.setTitle(parseTitle(strings));
        report.setStudentUserName(userName);
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

    public UserStatistic getUserStats(String userName) {
        Integer studyDays = reportRepository.findTotalStudyDays(userName);
        Integer totalHours = reportRepository.findTotalHours(userName);
        Report firstReport =
                reportRepository.findAllByStudentUserNameIgnoreCase(userName).stream()
                        .filter(this::hasRequiredFields)
                        .min(Comparator.comparing(Report::getDate))
                        .orElse(Report.builder()
                                .date(LocalDate.now())
                                .build()
                        );
        long totalDays = DAYS.between(firstReport.getDate(), LocalDate.now(ZoneId.of("Europe/Moscow")));

        return UserStatistic.builder()
                .userName(userName)
                .totalDays((int) totalDays)
                .studyDays(studyDays)
                .totalHours(totalHours)
                .averagePerWeek((float) totalHours * 7.f / totalDays)
                .build();
    }

    public List<UserStatistic> getAllUsersStats() {
        return reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .distinct()
                .map(this::getUserStats)
                .collect(Collectors.toList());
    }

    private boolean hasRequiredFields(Report report) {
        return report.getDate() != null && report.getHours() != null && report.getTitle() != null;
    }
}
