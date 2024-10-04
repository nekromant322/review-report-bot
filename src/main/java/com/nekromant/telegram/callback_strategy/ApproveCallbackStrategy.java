package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.callback_strategy.utils.StrategyUtils;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_APPROVED;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_BOOKED;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Component
public class ApproveCallbackStrategy implements CallbackStrategy {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private StrategyUtils strategyUtils;

    @Override
    public void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);
        SendMessage messageForMentors = messageMap.get(ChatType.MENTORS_CHAT);

        String callbackData = update.getCallbackQuery().getData();
        Long reviewId = Long.parseLong(callbackData.split(" ")[1]);
        int timeSlot = Integer.parseInt(callbackData.split(" ")[2]);

        ReviewRequest review = strategyUtils.getReviewRequest(reviewId);
        messageForUser.setChatId(review.getStudentChatId());
        LocalDateTime timeSlotDateTime = LocalDateTime.of(review.getDate(), LocalTime.of(timeSlot, 0));
        String mentorUsername = update.getCallbackQuery().getFrom().getUserName();
        if (reviewRequestRepository.existsByBookedDateTimeAndMentorUserName(timeSlotDateTime,
                mentorUsername)) {
            setMessageTextForMentorsTaken(messageForMentors, timeSlot, mentorUsername);
        } else  {
            bookTimeSlot(update, review, timeSlotDateTime);

            setMessageTextForUserApproved(messageForUser, review);
            setMessageTextForMentorsApproved(messageForMentors, update, review);
        }
        deleteMessageStrategy.setMessagePart(MessagePart.MARKUP);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.APPROVE;
    }

    private void bookTimeSlot(Update update, ReviewRequest review, LocalDateTime timeSlotDateTime) {
        setBookedDateTime(review, timeSlotDateTime);
        setMentorUserName(review, update);
        saveReviewRequest(review);
    }

    private void setMessageTextForMentorsTaken(SendMessage messageForMentors, int timeSlot, String mentorUsername) {
        messageForMentors.setText(String.format("%d уже забронировано для ментора %s", timeSlot, mentorUsername));
    }

    private void setBookedDateTime(ReviewRequest review, LocalDateTime timeSlotLDT) {
        review.setBookedDateTime(timeSlotLDT);
    }

    private void setMentorUserName(ReviewRequest review, Update update) {
        review.setMentorUserName(update.getCallbackQuery().getFrom().getUserName());
    }

    private void saveReviewRequest(ReviewRequest review) {
        reviewRequestRepository.save(review);
    }

    private void setMessageTextForUserApproved(SendMessage messageForUser, ReviewRequest review) {
        messageForUser.setText(String.format(REVIEW_BOOKED, review.getMentorUserName(),
                review.getBookedDateTime().format(defaultDateTimeFormatter()), review.getTitle()));
    }

    private void setMessageTextForMentorsApproved(SendMessage messageForMentors, Update update, ReviewRequest review) {
        messageForMentors.setText(String.format(REVIEW_APPROVED, update.getCallbackQuery().getFrom().getUserName(),
                review.getStudentUserName(), review.getBookedDateTime().format(defaultDateTimeFormatter())));
    }
}
