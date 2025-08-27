package com.nekromant.telegram.service;


import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.MessageContants.NO_REVIEW_TODAY;
import static com.nekromant.telegram.utils.FormatterUtils.*;

@Service
public class ReviewScheduleService {
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private TimezoneService timezoneService;

    public String getSchedule(UserInfo user) {
        LocalDateTime todayDate = LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay();
        LocalDateTime dateLastReview = reviewRequestRepository.findFirstByOrderByBookedDateTimeDesc().getBookedDateTime();
        if (todayDate.isBefore(dateLastReview)) {
            return formatHeader(todayDate, dateLastReview) + scheduleContentFormatter(user, todayDate, dateLastReview);
        } else {
            return formatHeader(todayDate, todayDate) + scheduleContentFormatter(user, todayDate, todayDate);
        }
    }

    public String getSchedule(UserInfo user, LocalDateTime fromDate, LocalDateTime toDate) {
        return formatHeader(fromDate, toDate) + scheduleContentFormatter(user, fromDate, toDate);
    }

    public String getScheduleToday(UserInfo user) {
        return "Расписание ревью на сегодня:\n" + scheduleContentFormatter(user, LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay(),
                LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay());
    }

    private String scheduleContentFormatter(UserInfo userInfo, LocalDateTime fromDate, LocalDateTime toDate) {
        List<ReviewRequest> reviewRequests = reviewRequestRepository.findAllByBookedDateTimeBetween(
                fromDate.toLocalDate().atStartOfDay(), toDate.toLocalDate().plusDays(1).atStartOfDay());
        if (reviewRequests.isEmpty()) {
            return NO_REVIEW_TODAY;
        }
        StringBuilder result = new StringBuilder();
        reviewRequests.sort(Comparator.comparing(ReviewRequest::getBookedDateTime));
        List<LocalDate> allDates = reviewRequests.stream()
                .map(ReviewRequest::getBookedDateTime)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());

        for (LocalDate date : allDates) {
            result.append(date.format(defaultDateFormatter()) + "\n");
            reviewRequests.stream()
                    .filter(reviewRequest -> reviewRequest.getBookedDateTime().toLocalDate().equals(date))
                    .forEach(review -> result.append("\n" +
                            "@" + review.getStudentInfo().getUserName() + "\n" +
                            timezoneService.convertToUserZone(review.getBookedDateTime(), userInfo).format(defaultTimeFormatter()) + "\n" +
                            review.getTitle() + "\n" +
                            "@" + review.getMentorInfo().getUserName() + "\n" +
                            mentorRepository.findMentorByMentorInfo(review.getMentorInfo()).getRoomUrl() + "\n"));

            if (!date.equals(allDates.get(allDates.size() - 1))) {
                result.append("<----------------------->\n");
            }
        }
        return result.toString();
    }

    private String formatHeader(LocalDateTime fromDate, LocalDateTime toDate) {
        return "Расписание всех ревью с " + fromDate.toLocalDate().format(defaultDateFormatter()) + " по "
                + toDate.toLocalDate().format(defaultDateFormatter()) + ":\n";
    }
}
