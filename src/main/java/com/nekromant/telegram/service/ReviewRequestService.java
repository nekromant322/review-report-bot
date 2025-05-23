package com.nekromant.telegram.service;

import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewRequestService {

    private static final String TITLE_BEGINS_WITH = "тема";
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private TimezoneService timezoneService;

    public ReviewRequest findReviewRequestById(Long id) {
        return reviewRequestRepository.findById(id).orElseThrow(InvalidParameterException::new);
    }

    public ReviewRequest save(ReviewRequest reviewRequest) {
        return reviewRequestRepository.save(reviewRequest);
    }

    public boolean existsByBookedDateTimeAndMentorUserInfo(LocalDateTime bookedDateTime, UserInfo mentorInfo) {
        return reviewRequestRepository.existsByBookedDateTimeAndMentorInfo(bookedDateTime, mentorInfo);
    }

    public ReviewRequest getTemporaryReviewRequest(User user, String[] arguments) {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setStudentInfo(userInfoService.getUserInfo(user.getId()));
        reviewRequest.setTitle(parseTitle(arguments));
        reviewRequest.setTimeSlots(timezoneService.parseTimeSlotsToMoscow(userInfoService.getUserInfo(user.getId()), parseTimeSlots(arguments)));

        return reviewRequest;
    }

    private Set<Integer> parseTimeSlots(String[] strings) {
        Set<Integer> timeSlots = new HashSet<>();
        for (String string : strings) {
            if (!string.toLowerCase().contains(TITLE_BEGINS_WITH)) {
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

    private String parseTitle(String[] strings) {
        String title = extractTitle(strings);
        isTitlePresent(title);
        return title;
    }

    private String extractTitle(String[] strings) {
        return Arrays.stream(strings)
                .dropWhile(s -> !s.toLowerCase().contains(TITLE_BEGINS_WITH))
                .skip(1)
                .collect(Collectors.joining(" "));
    }

    private void isTitlePresent(String title) {
        if (title.isEmpty()) {
            throw new InvalidParameterException("Не указана тема ревью.");
        }
    }
}
