package com.nekromant.telegram.controller;

import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SchedulePeriodService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class IncomingReviewRestController {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private SchedulePeriodService schedulePeriodService;

    @GetMapping("/incoming-review-with-period")
    public List<BookedReviewDTO> getIncomingReviewWithPeriod(@RequestParam String mentor) {
        Long startTime = schedulePeriodService.getStart();
        Long endTime = schedulePeriodService.getEnd();
        LocalDateTime startDateTime = LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay().plusHours(startTime);
        LocalDateTime endDateTime = startDateTime.plusHours(endTime <= 12 ? (24 + endTime - startTime) : (endTime - startTime));

        Stream<BookedReviewDTO> streamReviews = reviewRequestRepository
                .findAllByBookedDateTimeBetween(startDateTime, endDateTime)
                .stream()
                .filter(x -> x.getMentorUserName().equals(mentor))
                .map(x -> new BookedReviewDTO(
                        x.getStudentUserName(),
                        "https://t.me/" + x.getStudentUserName(),
                        x.getMentorUserName(),
                        x.getTitle().replace("Тема:", ""),
                        x.getBookedDateTime().toString().replace("T", " "),
                        false,
                        mentorRepository.findMentorByUserName(x.getMentorUserName()).getRoomUrl(),
                        true
                ))
                .sorted(Comparator.comparing(BookedReviewDTO::getBookedDateTime));

        return streamReviews.collect(Collectors.toList());
    }

    @Data
    public class BookedReviewDTO {
        private String studentUserName;
        private String studentTgLink;
        private String mentorUserName;
        private String title;
        private String bookedDateTime;
        private boolean tooLate;
        private String roomLink;
        private boolean today;

        public BookedReviewDTO(String studentUserName, String studentTgLink, String mentorUserName, String title,
                               String bookedDateTime, boolean tooLate, String roomLink, boolean today) {
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
