package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.nekromant.telegram.contants.MessageContants.NO_REVIEW_TODAY;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static com.nekromant.telegram.utils.FormatterUtils.defaultTimeFormatter;
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
    private MentorRepository mentorRepository;

    @Test
    void getSchedule_OneDay_returnCorrectSchedule() {
        //Arrange
        LocalDateTime date = LocalDateTime.of(2025, 12, 12, 12, 12);
        String formattedDate = date.format(defaultDateFormatter());
        ReviewRequest reviewRequest = createMockReviewRequest(date);
        Mentor mentor = new Mentor();
        mentor.setRoomUrl("test_url");
        when(mentorRepository.findMentorByMentorInfo(any())).thenReturn(mentor);
        when(reviewRequestRepository.findAllByBookedDateTimeBetween(any(), any()))
                .thenReturn(new ArrayList<>(List.of(reviewRequest)));
        //Act
        String result = reviewScheduleService.getSchedule(new UserInfo(), date, date);
        //Assert
        assertThat(result).contains(String.format("Расписание всех ревью с %s по %s:\n", formattedDate, formattedDate));
        assertThat(result).contains("@test_user");
        assertThat(result).contains("@test_mentor");
        assertThat(result).contains(reviewRequest.getBookedDateTime().format(defaultTimeFormatter()));

    }

    @Test
    void getSchedule_NoReview_returnNoReviewSchedule() {
        //Arrange
        LocalDateTime date = LocalDateTime.of(2025, 12, 12, 12, 12);
        String formattedDate = date.format(defaultDateFormatter());
        when(reviewRequestRepository.findAllByBookedDateTimeBetween(any(), any()))
                .thenReturn(new ArrayList<>()); // empty reviewlist
        //Act
        String result = reviewScheduleService.getSchedule(new UserInfo(), date, date);
        //Assert
        assertThat(result).contains(String.format("Расписание всех ревью с %s по %s:\n", formattedDate, formattedDate));
        assertThat(result).contains(NO_REVIEW_TODAY);
    }

    @Test
    void getSchedule_ToDateBeforeFromDate_returnTodaySchedule(){
        //Arrange
        LocalDateTime fromDate = LocalDateTime.of(2025, 12, 12, 12, 12);
        LocalDateTime toDate = LocalDateTime.of(2024, 11, 11, 11, 11);
        String formattedDate = fromDate.format(defaultDateFormatter());
        ReviewRequest reviewRequest = createMockReviewRequest(fromDate);
        Mentor mentor = new Mentor();
        mentor.setRoomUrl("test_url");
        when(mentorRepository.findMentorByMentorInfo(any())).thenReturn(mentor);
        when(reviewRequestRepository.findAllByBookedDateTimeBetween(any(), any()))
                .thenReturn(new ArrayList<>(List.of(reviewRequest)));
        //Act
        String result = reviewScheduleService.getSchedule(new UserInfo(), fromDate, toDate);
        //Assert
        assertThat(result).contains(String.format("Расписание всех ревью с %s по %s:\n", formattedDate, formattedDate));
        assertThat(result).contains("@test_user");
        assertThat(result).contains("@test_mentor");
        assertThat(result).contains(reviewRequest.getBookedDateTime().format(defaultTimeFormatter()));
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