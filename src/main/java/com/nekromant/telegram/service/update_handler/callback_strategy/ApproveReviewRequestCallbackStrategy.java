package com.nekromant.telegram.service.update_handler.callback_strategy;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.ReviewRequestService;
import com.nekromant.telegram.service.TimezoneService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_APPROVED;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_BOOKED;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Component
public class ApproveReviewRequestCallbackStrategy implements CallbackStrategy {

    private static final int MIDNIGHT = 24;
    @Autowired
    private ReviewRequestService reviewRequestService;
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private TimezoneService timezoneService;

    @Override
    public void executeCallbackQuery(CallbackQuery callbackQuery, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);
        SendMessage messageForMentors = messageMap.get(ChatType.MENTORS_CHAT);

        String callbackData = callbackQuery.getData();
        Long reviewId = Long.parseLong(callbackData.split(" ")[1]);
        int timeSlot = Integer.parseInt(callbackData.split(" ")[2]);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(callbackData.split(" ")[3], formatter);

        ReviewRequest review = reviewRequestService.findReviewRequestById(reviewId);
        messageForUser.setChatId(review.getStudentInfo().getChatId().toString());
        LocalDateTime timeSlotDateTime;
        if (timeSlot == MIDNIGHT) {
            timeSlotDateTime = LocalDateTime.of(date, LocalTime.of(0, 0));
        } else {
            timeSlotDateTime = LocalDateTime.of(date, LocalTime.of(timeSlot, 0));
        }
        UserInfo mentorInfo = userInfoService.getUserInfo(callbackQuery.getFrom().getId());
        if (reviewRequestService.existsByBookedDateTimeAndMentorUserInfo(timeSlotDateTime,
                mentorInfo)) {
            setMessageTextForMentorsTaken(messageForMentors, timeSlot, mentorInfo.getUserName());
        } else {
            bookTimeSlot(callbackQuery, review, timeSlotDateTime);
            review.setTimeSlots(Set.of(timeSlot));
            saveReviewRequest(review);

            setMessageTextForUserApproved(messageForUser, review);
            setMessageTextForMentorsApproved(messageForMentors, callbackQuery, review);
        }
        deleteMessageStrategy.setMessagePart(MessagePart.MARKUP);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.APPROVE_REVIEW_REQUEST;
    }

    private void bookTimeSlot(CallbackQuery callbackQuery, ReviewRequest review, LocalDateTime timeSlotDateTime) {
        setBookedDateTime(review, timeSlotDateTime);
        setMentor(review, callbackQuery);
    }

    private void setMessageTextForMentorsTaken(SendMessage messageForMentors, int timeSlot, String mentorUsername) {
        messageForMentors.setText(String.format("%d уже забронировано для ментора %s", timeSlot, mentorUsername));
    }

    private void setBookedDateTime(ReviewRequest review, LocalDateTime timeSlotLDT) {
        review.setBookedDateTime(timeSlotLDT);
    }

    private void setMentor(ReviewRequest review, CallbackQuery callbackQuery) {
        review.setMentorInfo(userInfoService.getUserInfo(callbackQuery.getFrom().getId()));
    }

    private void saveReviewRequest(ReviewRequest review) {
        reviewRequestService.save(review);
    }

    private void setMessageTextForUserApproved(SendMessage messageForUser, ReviewRequest review) {
        LocalDateTime dateTime = timezoneService.convertToUserZone(review.getBookedDateTime(), review.getStudentInfo());
        messageForUser.setText(String.format(REVIEW_BOOKED, review.getMentorInfo().getUserName(),
                dateTime.format(defaultDateTimeFormatter()), review.getTitle()));
    }

    private void setMessageTextForMentorsApproved(SendMessage messageForMentors, CallbackQuery callbackQuery, ReviewRequest review) {
        messageForMentors.setText(String.format(REVIEW_APPROVED, callbackQuery.getFrom().getUserName(),
                review.getStudentInfo().getUserName(), review.getBookedDateTime().format(defaultDateTimeFormatter())));
    }
}
