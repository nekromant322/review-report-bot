package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewScheduleServiceTest {
    @InjectMocks
    private ReviewScheduleService reviewScheduleService;
    @Mock
    private ReviewRequestRepository reviewRequestRepository;
    @Mock
    private UserInfoService userInfoService;
    @Mock
    private MentorRepository mentorRepository;

    @ParameterizedTest
    @MethodSource("provideDateListArguments")
    void getSchedule_AnyDates_returnCorrectSchedule(LocalDateTime fromDate, LocalDateTime toDate) {
        //Arrange
        ReviewRequest reviewRequest = createMockReviewRequest(fromDate);
        Mentor mentor = new Mentor();
        mentor.setRoomUrl("test_url");
        when(mentorRepository.findMentorByMentorInfo(any())).thenReturn(mentor);
        when(reviewRequestRepository.findAllByBookedDateTimeBetween(any(), any()))
                .thenReturn(new ArrayList<>(List.of(reviewRequest)));
        //Act
        //Assert
        Assertions.assertDoesNotThrow(() -> reviewScheduleService.getSchedule(new UserInfo(), fromDate, toDate));
    }

    @Test
    void getScheduleToday_AnyDates_returnTodaySchedule() {
        //Arrange
        LocalDateTime fromDate = LocalDateTime.of(2025, 12, 12, 12, 12);
        ReviewRequest reviewRequest = createMockReviewRequest(fromDate);
        Mentor mentor = new Mentor();
        mentor.setRoomUrl("test_url");
        when(mentorRepository.findMentorByMentorInfo(any())).thenReturn(mentor);
        when(reviewRequestRepository.findAllByBookedDateTimeBetween(any(), any()))
                .thenReturn(new ArrayList<>(List.of(reviewRequest)));

        //Act
        String result = reviewScheduleService.getScheduleToday(new UserInfo());

        //Assert
        assertThat(result).contains("Расписание ревью на сегодня:");
        assertThat(result).contains("@test_user");
        assertThat(result).contains("@test_mentor");
        assertThat(result).contains("12:12");
    }


    private static Stream<Arguments> provideDateListArguments() {
        return Stream.of(
                Arguments.of(LocalDateTime.of(2025, 12, 12, 12, 12), LocalDateTime.of(2026, 11, 11, 11, 11)),
                Arguments.of(LocalDateTime.of(2025, 12, 12, 12, 12), LocalDateTime.of(2025, 12, 12, 12, 12)),
                Arguments.of(LocalDateTime.of(2026, 11, 11, 11, 11), LocalDateTime.of(2025, 12, 12, 12, 12))
        );
    }

    private ReviewRequest createMockReviewRequest(LocalDateTime date) {
        UserInfo user = new UserInfo();
        user.setUserName("test_user");
        UserInfo mentorInfo = new UserInfo();
        mentorInfo.setUserName("test_mentor");
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setBookedDateTime(date);
        reviewRequest.setTitle("test_title");
        reviewRequest.setStudentInfo(user);
        reviewRequest.setMentorInfo(mentorInfo);
        return reviewRequest;
    }
}