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
                reportRepository.findAll().stream().sorted(Comparator.comparing(Report::getDate)).findFirst()
                        .orElse(Report.builder()
                                .date(LocalDate.now())
                                .build()
                        );
        long totalDays = DAYS.between(firstReport.getDate(), LocalDate.now(ZoneId.of("Europe/Moscow"))) + 1;

        return UserStatistic.builder()
                .userName(userName)
                .totalDays((int) totalDays)
                .studyDays(studyDays)
                .totalHours(totalHours)
                .averagePerWeek((float) totalHours / 7.f)
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
}
