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

import java.util.Map;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.MessageContants.NOBODY_CAN_MAKE_REVIEW;
import static com.nekromant.telegram.contants.MessageContants.SOMEBODY_DENIED_REVIEW;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class DenyReviewRequestCallbackStrategy implements CallbackStrategy {

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

        ReviewRequest review = strategyUtils.getReviewRequest(reviewId);
        messageForUser.setChatId(review.getStudentChatId());

        setMessageTextDenied(messageForUser, review);
        setMessageTextDeniedForMentors(messageForMentors, update, review);

        reviewRequestRepository.deleteById(reviewId);
        deleteMessageStrategy.setMessagePart(MessagePart.MARKUP);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.DENY_REVIEW_REQUEST;
    }

    private void setMessageTextDenied(SendMessage messageForUser, ReviewRequest review) {
        messageForUser.setText(String.format(NOBODY_CAN_MAKE_REVIEW, review.getDate().format(defaultDateFormatter())) +
                review.getTimeSlots().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(":00, ")) + ":00" + "\n");
    }

    private void setMessageTextDeniedForMentors(SendMessage messageForMentors, Update update, ReviewRequest review) {
        messageForMentors.setText(String.format(SOMEBODY_DENIED_REVIEW, update.getCallbackQuery().getFrom().getUserName(),
                review.getStudentUserName()));
    }
}
