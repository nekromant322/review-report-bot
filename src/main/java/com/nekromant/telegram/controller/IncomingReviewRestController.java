package com.nekromant.telegram.controller;

import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class IncomingReviewRestController {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private MentorRepository mentorRepository;

    @GetMapping("/incoming-review")
    public List<BookedReviewDTO> getIncomingReview() {
        LocalDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();
        return reviewRequestRepository
                .findAllByBookedDateTimeBetween(nowInMoscow.minus(2, ChronoUnit.HOURS), nowInMoscow.plus(2, ChronoUnit.DAYS))
                .stream()
                .map(x -> new BookedReviewDTO(
                        x.getStudentUserName(),
                        "https://t.me/" + x.getStudentUserName(),
                        x.getMentorUserName(),
                        x.getTitle().replace("Тема:", ""),
                        x.getBookedDateTime(),
                        nowInMoscow.isAfter(x.getBookedDateTime()),
                        mentorRepository.findMentorByUserName(x.getMentorUserName()).getRoomUrl(),
                        nowInMoscow.plus(20, ChronoUnit.HOURS).isBefore(x.getBookedDateTime())
                ))
                .sorted(Comparator.comparing(BookedReviewDTO::getBookedDateTime))
                .collect(Collectors.toList());
    }

    @Data
    public class BookedReviewDTO {
        private String studentUserName;
        private String studentTgLink;
        private String mentorUserName;
        private String title;
        private LocalDateTime bookedDateTime;
        private boolean tooLate;
        private String roomLink;
        private boolean today;

        public BookedReviewDTO(String studentUserName, String studentTgLink, String mentorUserName, String title,
                               LocalDateTime bookedDateTime, boolean tooLate, String roomLink, boolean today) {
            this.studentUserName = studentUserName;
            this.studentTgLink = studentTgLink;
            this.mentorUserName = mentorUserName;
            this.title = title;
            this.bookedDateTime = bookedDateTime;
            this.tooLate = tooLate;
            this.roomLink = roomLink;
            this.today = today;
        }
    }
}
