package com.nekromant.telegram.model;

import com.nekromant.telegram.converter.TimeSlotsConverter;
import lombok.Data;
import lombok.ToString;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.persistence.*;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@ToString
public class ReviewRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String studentChatId;

    @Column
    private String studentUserName;

    @Column
    private String mentorUserName;

    @Column
    private String title;

    @Column
    private LocalDate date;

    @Convert(converter = TimeSlotsConverter.class)
    @Column
    private Set<Integer> timeSlots;

    @Column
    private LocalDateTime bookedDateTime;

    public static ReviewRequest getTemporaryReviewRequest(User user, String[] arguments, String studentChatId) {
        ReviewRequest reviewRequest = new ReviewRequest();

        reviewRequest.setStudentUserName(user.getUserName());
        reviewRequest.setStudentChatId(studentChatId);
        reviewRequest.setTitle(parseTitle(arguments));
        reviewRequest.setTimeSlots(parseTimeSlots(arguments));
        return reviewRequest;
    }

    private static Set<Integer> parseTimeSlots(String[] strings) {
        Set<Integer> timeSlots = new HashSet<>();
        for (String string : strings) {
            if (!string.toLowerCase().contains("тема")) {
                timeSlots.add(Integer.parseInt(string));
                if (Integer.parseInt(string) > 24 || Integer.parseInt(string) < 0) {
                    throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 23");
                }
            } else {
                return timeSlots;
            }
        }
        return timeSlots;
    }

    private static String parseTitle(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].toLowerCase().contains("тема")) {
                return Arrays.stream(strings).skip(i).collect(Collectors.joining(" "));
            }
        }
        return "";
    }
}
