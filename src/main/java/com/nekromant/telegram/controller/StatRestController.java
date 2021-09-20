package com.nekromant.telegram.controller;

import com.nekromant.telegram.contants.Step;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.Salary;
import com.nekromant.telegram.model.StepPassed;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.repository.SalaryRepository;
import com.nekromant.telegram.repository.StepPassedRepository;
import com.nekromant.telegram.service.ActualStatPhotoHolderService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;

@RestController
public class StatRestController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ActualStatPhotoHolderService actualStatPhotoHolderService;

    @Autowired
    private StepPassedRepository stepPassedRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    private static final List<String> colors = new ArrayList<>();

    static {
        colors.add("rgba(255, 8, 8, 1)");
        colors.add("rgba(255, 127, 8, 1)");
        colors.add("rgba(255, 247, 8, 1)");
        colors.add("rgba(103, 255, 8, 1)");
        colors.add("rgba(8, 255, 234, 1)");
        colors.add("rgba(16, 8, 255, 1)");
        colors.add("rgba(119, 8, 255, 1)");
        colors.add("rgba(230, 8, 255, 1)");
        colors.add("rgba(230, 8, 255, 1)");
        colors.add("rgba(255, 8, 144, 1)");
        colors.add("rgba(0, 0, 0, 1)");
        colors.add("rgba(37, 44, 69, 1)");
    }

    @GetMapping("/statPerDay")
    public Stat getStatPerDay() {
        LocalDate firstReviewDate = getFirstReviewDate();
        List<LocalDate> labels = Stream.iterate(firstReviewDate, date -> date.plus(1, DAYS))
                .limit(DAYS.between(firstReviewDate, LocalDate.now()) + 1).collect(Collectors.toList());

        List<UserStat> userStats = new ArrayList<>();

        List<String> allUserNames = getAllUserNamesWIthReports();

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
                .labels(labels.stream().map(LocalDate::toString).collect(Collectors.toList()))
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

        List<String> allUserNames = getAllUserNamesWIthReports();

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
                .labels(labels.stream().map(LocalDate::toString).collect(Collectors.toList()))
                .userStats(userStats)
                .build();
    }

    @GetMapping("/statStep")
    public Stat getStepStat() {
        List<Step> labels = Arrays.stream(Step.values())
                .skip(1) //begin step
                .collect(Collectors.toList());

        List<UserStat> userStats = new ArrayList<>();

        List<String> allUserNames = getAllUserNamesWIthStepPassed();

        int colorNumber = 0;
        for (String userName : allUserNames) {
            List<Integer> daysForSteps = new ArrayList<>();
            List<StepPassed> allStudentPassedSteps = stepPassedRepository.findAllByStudentUserName(userName).stream()
                    .sorted(Comparator.comparing(StepPassed::getDate))
                    .collect(Collectors.toList());

            for (int i = 0; i < allStudentPassedSteps.size() - 1; i++) {
                long countDays = DAYS.between(allStudentPassedSteps.get(i).getDate(), allStudentPassedSteps.get(i + 1).getDate());
                daysForSteps.add((int) countDays);
            }
            userStats.add(UserStat.builder()
                    .label(userName)
                    .data(daysForSteps)
                    .borderColor(colors.get(colorNumber % colors.size()))
                    .backgroundColor(colors.get(colorNumber++ % colors.size()))
                    .borderWidth(1)
                    .build());
        }

        return Stat.builder()
                .labels(labels.stream().map(Enum::name).collect(Collectors.toList()))
                .userStats(userStats.stream()
                        .filter(stat -> stat.getData().size() > 0)
                        .sorted((stat1, stat2) -> stat2.getData().size() - stat1.getData().size())
                        .collect(Collectors.toList()))
                .build();
    }

    @GetMapping("/statSalary")
    public Stat getStatSalary() {
        LocalDate firstSalaryDate = salaryRepository.findAll().stream().min(Comparator.comparing(Salary::getDate)).get().getDate();


        List<LocalDate> labels = Stream.iterate(firstSalaryDate, date -> date.plus(1, MONTHS))
                .limit(MONTHS.between(firstSalaryDate, LocalDate.now()) + 1)
                .collect(Collectors.toList());

        List<UserStat> userStats = new ArrayList<>();

        List<String> allUserNames = salaryRepository.findAll()
                .stream()
                .map(Salary::getUserName)
                .distinct()
                .collect(Collectors.toList());

        int colorNumber = 0;
        for (String userName : allUserNames) {
            List<Integer> salaries = new ArrayList<>();
            List<Salary> allStudentSalaries = salaryRepository.findAllByUserName(userName);
            for (LocalDate label : labels) {

                int salaryIfNotMatched = salaries.size() > 0 ? salaries.get(salaries.size() - 1) : 0;
                Integer salaryForLabel = allStudentSalaries.stream()
                        .filter(salary -> salary.getDate().getYear() == label.getYear() &&
                                salary.getDate().getMonthValue() == label.getMonthValue())
                        .map(Salary::getSalary)
                        .findFirst()
                        .orElse(salaryIfNotMatched);

                salaries.add(salaryForLabel);
            }
            userStats.add(UserStat.builder()
                    .label(userName)
                    .data(salaries)
                    .borderColor(colors.get(colorNumber++ % colors.size()))
                    .backgroundColor("rgba(255, 99, 132, 0)")
                    .borderWidth(1)
                    .steppedLine(true)
                    .build());
        }

        return Stat.builder()
                .labels(labels.stream().map(LocalDate::toString).collect(Collectors.toList()))
                .userStats(userStats)
                .build();
    }


    @PostMapping("/updatePerDayPhoto")
    public void setPerDayGraphEncodedPhoto(@RequestBody PhotoData photoData) {
        actualStatPhotoHolderService.setEncodedPerDayGraph(photoData.encodedPhoto.replaceFirst("^.*;base64,", ""));
    }

    private LocalDate getFirstReviewDate() {
        return reportRepository.findAll().stream().min(Comparator.comparing(Report::getDate)).get().getDate();
    }

    private List<String> getAllUserNamesWIthReports() {
        return reportRepository.findAll()
                .stream()
                .map(Report::getStudentUserName)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getAllUserNamesWIthStepPassed() {
        return stepPassedRepository.findAll()
                .stream()
                .map(StepPassed::getStudentUserName)
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
        private List<String> labels;
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
        private Boolean steppedLine = false;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class PhotoData {
        private String encodedPhoto;
    }
}
