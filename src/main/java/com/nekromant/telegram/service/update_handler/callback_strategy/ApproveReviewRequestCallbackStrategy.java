package com.nekromant.telegram.service.update_handler.callback_strategy;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.ReviewRequestService;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_APPROVED;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_BOOKED;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Component
public class ApproveReviewRequestCallbackStrategy implements CallbackStrategy {

    private static final int MIDNIGHT = 24;
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private ReviewRequestService reviewRequestService;

    @Override
    public void executeCallbackQuery(CallbackQuery callbackQuery, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);
        SendMessage messageForMentors = messageMap.get(ChatType.MENTORS_CHAT);

        String callbackData = callbackQuery.getData();
        Long reviewId = Long.parseLong(callbackData.split(" ")[1]);
        int timeSlot = Integer.parseInt(callbackData.split(" ")[2]);

        ReviewRequest review = reviewRequestService.findReviewRequestById(reviewId);
        messageForUser.setChatId(review.getStudentChatId());
        LocalDateTime timeSlotDateTime;
        if (timeSlot == MIDNIGHT) {
            timeSlotDateTime = LocalDateTime.of(review.getDate().plusDays(1), LocalTime.of(0, 0));
        } else {
            timeSlotDateTime = LocalDateTime.of(review.getDate(), LocalTime.of(timeSlot, 0));
        }
        String mentorUsername = callbackQuery.getFrom().getUserName();
        if (reviewRequestRepository.existsByBookedDateTimeAndMentorUserName(timeSlotDateTime,
                mentorUsername)) {
            setMessageTextForMentorsTaken(messageForMentors, timeSlot, mentorUsername);
        } else  {
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
        setMentorUserName(review, callbackQuery);
    }

    private void setMessageTextForMentorsTaken(SendMessage messageForMentors, int timeSlot, String mentorUsername) {
        messageForMentors.setText(String.format("%d уже забронировано для ментора %s", timeSlot, mentorUsername));
    }

    private void setBookedDateTime(ReviewRequest review, LocalDateTime timeSlotLDT) {
        review.setBookedDateTime(timeSlotLDT);
    }

    private void setMentorUserName(ReviewRequest review, CallbackQuery callbackQuery) {
        review.setMentorUserName(callbackQuery.getFrom().getUserName());
    }

    private void saveReviewRequest(ReviewRequest review) {
        reviewRequestRepository.save(review);
    }

    private void setMessageTextForUserApproved(SendMessage messageForUser, ReviewRequest review) {
        messageForUser.setText(String.format(REVIEW_BOOKED, review.getMentorUserName(),
                review.getBookedDateTime().format(defaultDateTimeFormatter()), review.getTitle()));
    }

    private void setMessageTextForMentorsApproved(SendMessage messageForMentors, CallbackQuery callbackQuery, ReviewRequest review) {
        messageForMentors.setText(String.format(REVIEW_APPROVED, callbackQuery.getFrom().getUserName(),
                review.getStudentUserName(), review.getBookedDateTime().format(defaultDateTimeFormatter())));
    }
}
