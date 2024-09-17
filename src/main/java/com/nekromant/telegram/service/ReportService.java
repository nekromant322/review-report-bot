package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

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
