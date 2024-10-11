package com.nekromant.telegram.model;

import lombok.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.persistence.*;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String studentUserName;

    @Column
    private Integer hours;

    @Column
    private LocalDate date;

    @Column
    private String title;

    public static void updateReportFromEditedMessage(String editedText, Report report) {
        String[] strings = editedText.split(" ");
        strings = Arrays.copyOfRange(strings, 1, strings.length);

        report.setHours(parseHours(strings));
        report.setTitle(parseTitle(strings));
    }

    public static Report getTemporaryReport(Update update) {
        String[] strings = update.getEditedMessage().getText().split(" ");
        strings = Arrays.copyOfRange(strings, 1, strings.length);

        String userName = update.getEditedMessage().getFrom().getUserName();
        return getTemporaryReport(strings, userName);
    }

    public static Report getTemporaryReport(String[] strings, String userName) {
        Report report = new Report();

        report.setHours(parseHours(strings));
        report.setTitle(parseTitle(strings));
        report.setStudentUserName(userName);
        return report;
    }

    private static int parseHours(String[] strings) {
        int newHours = Integer.parseInt(strings[0]);
        validateHoursArgument(newHours);
        return newHours;
    }

    private static String parseTitle(String[] strings) {
        return Arrays.stream(strings).skip(1).collect(Collectors.joining(" "));
    }

    private static void validateHoursArgument(int hours) {
        if (hours < 0 || hours > 24) {
            throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 24");
        }
    }
}
