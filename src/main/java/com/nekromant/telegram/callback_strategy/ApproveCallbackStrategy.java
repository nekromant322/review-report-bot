package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.callback_strategy.utils.StrategyUtils;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    public void executeCallbackQuery(Update update, String callbackData, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        Long reviewId = Long.parseLong(callbackData.split(" ")[1]);
        int timeSlot = Integer.parseInt(callbackData.split(" ")[2]);

        ReviewRequest review = strategyUtils.getReviewRequest(reviewId);
        messageForUser.setChatId(review.getStudentChatId());
        LocalDateTime timeSlotLDT = LocalDateTime.of(review.getDate(), LocalTime.of(timeSlot, 0));
        String mentorUsername = update.getCallbackQuery().getFrom().getUserName();
        if (reviewRequestRepository.existsByBookedDateTimeAndMentorUserName(timeSlotLDT,
                mentorUsername)) {
            setMessageTextForMentorsTaken(messageForMentors, timeSlot, mentorUsername);
        } else {
            setBookedDateTime(review, timeSlotLDT);
            setMentorUserName(review, update);
            saveReviewRequest(review);

            setMessageTextForUserApproved(messageForUser, review);
            setMessageTextForMentorsApproved(messageForMentors, update, review);
        }
        deleteMessageStrategy.setDeleteMessageStrategy(DeleteMessageStrategy.MARKUP);
    }

    private void setMessageTextForMentorsTaken(SendMessage messageForMentors, int timeSlot, String mentorUsername) {
        messageForMentors.setText(timeSlot + " уже забронировано для ментора " + mentorUsername);
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
