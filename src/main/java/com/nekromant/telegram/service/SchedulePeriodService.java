package com.nekromant.telegram.service;

import com.nekromant.telegram.dto.BookedReviewDTO;
import com.nekromant.telegram.model.SchedulePeriod;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.repository.SchedulePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SchedulePeriodService {
    @Value("${schedulePeriod.start}")
    private Long defaultStart;
    @Value("${schedulePeriod.end}")
    private Long defaultEnd;

    @Autowired
    private SchedulePeriodRepository schedulePeriodRepository;
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private MentorRepository mentorRepository;

    private Map<String, Long> period = new HashMap<>();

    @PostConstruct
    public void setSchedulePeriod() {
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);
        Long start;
        Long end;

        if (schedulePeriod != null) {
            start = schedulePeriod.getStartTime();
            end = schedulePeriod.getEndTime();
        } else {
            start = defaultStart;
            end = defaultEnd;
        }

        period.put("start", start);
        period.put("end", end);
    }

    public List<BookedReviewDTO> getBookedReviewList(String mentor) {
        Long startTime = getStart();
        Long endTime = getEnd();
        LocalDateTime startDateTime = LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay().plusHours(startTime);
        LocalDateTime endDateTime = startDateTime.plusHours(endTime <= 12 ? (24 + endTime - startTime) : (endTime - startTime));

        Stream<BookedReviewDTO> streamReviews = reviewRequestRepository
                .findAllByBookedDateTimeBetween(startDateTime, endDateTime)
                .stream()
                .filter(x -> x.getMentorInfo().getUserName().equals(mentor))
                .map(x -> new BookedReviewDTO(
                        x.getStudentInfo().getUserName(),
                        "https://t.me/" + x.getStudentInfo().getUserName(),
                        x.getMentorInfo().getUserName(),
                        x.getTitle().replace("Тема:", ""),
                        x.getBookedDateTime().toString().replace("T", " "),
                        false,
                        mentorRepository.findMentorByMentorInfo(x.getMentorInfo()).getRoomUrl(),
                        true
                ))
                .sorted(Comparator.comparing(BookedReviewDTO::getBookedDateTime));

        return streamReviews.collect(Collectors.toList());
    }

    private Long getStart() {
        return period.get("start");
    }

    private Long getEnd() {
        return period.get("end");
    }

    public void setStart(Long start) {
        period.put("start", start);
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);

        if (schedulePeriod != null) {
            schedulePeriod.setStartTime(start);
            schedulePeriodRepository.save(schedulePeriod);
        } else {
            SchedulePeriod newSchedulePeriod = new SchedulePeriod();
            newSchedulePeriod.setId(1L);
            newSchedulePeriod.setStartTime(start);
            newSchedulePeriod.setEndTime(defaultEnd);
            schedulePeriodRepository.save(newSchedulePeriod);
        }
    }

    public void setEnd(Long end) {
        period.put("end", end);
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);

        if (schedulePeriod != null) {
            schedulePeriod.setEndTime(end);
            schedulePeriodRepository.save(schedulePeriod);
        } else {
            SchedulePeriod newSchedulePeriod = new SchedulePeriod();
            newSchedulePeriod.setId(1L);
            newSchedulePeriod.setStartTime(defaultStart);
            newSchedulePeriod.setEndTime(end);
            schedulePeriodRepository.save(newSchedulePeriod);
        }
    }
}
