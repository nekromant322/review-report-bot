package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;

@RestController
public class StatRestController {

    @Autowired
    private ReportRepository reportRepository;

    private static final List<String> colors = new ArrayList<>();

    static {
        colors.add("rgba(255, 99, 132, 1)");
        colors.add("rgba(54, 162, 235, 1)");
        colors.add("rgba(255, 206, 86, 1)");
        colors.add("rgba(75, 192, 192, 1)");
        colors.add("rgba(153, 102, 255, 1)");
        colors.add("rgba(255, 159, 64, 1)");
        colors.add("rgb(54,255,0)");
        colors.add("rgb(0,27,113)");
        colors.add("rgb(255,0,0)");
        colors.add("rgb(0,150,102)");
        colors.add("rgb(100,150,102)");
        colors.add("rgb(100,50,150)");
        colors.add("rgb(100,100,100)");
        colors.add("rgb(200,200,200)");
    }

    @GetMapping("/statPerDay")
    public Stat getStatPerDay() {
        LocalDate firstReviewDate = getFirstReviewDate();
        List<LocalDate> labels = Stream.iterate(firstReviewDate, date -> date.plus(1, DAYS))
                .limit(DAYS.between(firstReviewDate, LocalDate.now()) + 1).collect(Collectors.toList());

        List<UserStat> userStats = new ArrayList<>();

        List<String> allUserNames = getAllUserNames();

        int colorNumber = 0;
        for (String userName : allUserNames) {
            List<Integer> hours = new ArrayList<>();
            List<Report> allStudentReports = reportRepository.findAllByStudentUserName(userName);
            for (LocalDate label : labels) {
                Report reportForLabel = findReportByLocalDate(allStudentReports, label);
                if (reportForLabel != null) {
                    hours.add(reportForLabel.getHours());
                } else {
                    hours.add(0);
                }
            }
            userStats.add(UserStat.builder()
                    .label(userName)
                    .data(hours)
                    .borderColor(colors.get(colorNumber++ % colors.size()))
                    .backgroundColor("rgba(255, 99, 132, 0)")
                    .borderWidth(1)
                    .build());
        }

        return Stat.builder()
                .labels(labels)
                .userStats(userStats)
                .build();
    }

    @GetMapping("/statPerWeek")
    public Stat getStatPerWeek() {
        LocalDate firstReviewDate = getFirstReviewDate();
        List<LocalDate> labels = Stream.iterate(firstReviewDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)), date -> date.plus(7, DAYS))
                .limit(WEEKS.between(firstReviewDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalDate.now()))
                .collect(Collectors.toList());

        List<UserStat> userStats = new ArrayList<>();

        List<String> allUserNames = getAllUserNames();

        int colorNumber = 0;
        for (String userName : allUserNames) {
            List<Integer> hours = new ArrayList<>();
            List<Report> allStudentReports = reportRepository.findAllByStudentUserName(userName);
            for (LocalDate label : labels) {
                hours.add(sumHoursPerWeekFromDate(allStudentReports, label));
            }
            userStats.add(UserStat.builder()
                    .label(userName)
                    .data(hours)
                    .borderColor(colors.get(colorNumber++ % colors.size()))
                    .backgroundColor("rgba(255, 99, 132, 0)")
                    .borderWidth(1)
                    .build());
        }

        return Stat.builder()
                .labels(labels)
                .userStats(userStats)
                .build();
    }

    public LocalDate getFirstReviewDate() {
        return reportRepository.findAll().stream().min(Comparator.comparing(Report::getDate)).get().getDate();
    }

    private List<String> getAllUserNames() {
        return reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .distinct()
                .collect(Collectors.toList());
    }

    private Report findReportByLocalDate(List<Report> allStudentReports, LocalDate localDate) {
        for (Report report : allStudentReports) {
            if (report.getDate().equals(localDate)) {
                return report;
            }
        }
        return null;
    }

    private int sumHoursPerWeekFromDate(List<Report> allStudentReports, LocalDate localDate) {
        int sum = 0;
        for (Report report : allStudentReports) {
            if (DAYS.between(localDate, report.getDate()) < 7) {
                sum += report.getHours();
            }
        }
        return sum;
    }

    @Data
    @Builder
    private static class Stat {
        private List<LocalDate> labels;
        private List<UserStat> userStats;
    }

    @Data
    @Builder
    private static class UserStat {
        private String label;
        private List<Integer> data;
        private String borderColor;
        private String backgroundColor;
        private Integer borderWidth;

    }
}
