package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.Map;

import static com.nekromant.telegram.contants.MessageContants.REVIEW_REQUEST_SENT;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Slf4j
@Component
public class SetReviewRequestDateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Override
    public CallBack getPrefix() {
        return CallBack.SET_REVIEW_REQUEST_DATE_TIME;
    }

    @Override
    public void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);

        setReviewRequestDate(update, messageForUser, deleteMessageStrategy);
    }

    private void setReviewRequestDate(Update update, SendMessage messageForUser, DeleteMessageStrategy deleteMessageStrategy) {
        String callbackData = update.getCallbackQuery().getData();
        String[] dataParts = callbackData.split(" ");
        String date = dataParts[1];
        Long reviewRequestId = Long.parseLong(callbackData.split(" ")[2]);
        LocalDate reviewRequestDate = LocalDate.parse(date, defaultDateFormatter());

        ReviewRequest reviewRequest = getReviewRequest(reviewRequestId);

        setReviewRequestDateAndSave(messageForUser, reviewRequest, reviewRequestDate);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    private void setReviewRequestDateAndSave(SendMessage messageForUser, ReviewRequest reviewRequest, LocalDate reviewRequestDate) {
        reviewRequest.setDate(reviewRequestDate);
        reviewRequestRepository.save(reviewRequest);

        messageForUser.setText(REVIEW_REQUEST_SENT);
    }

    private ReviewRequest getReviewRequest(Long reviewRequestId) {
        return reviewRequestRepository.findById(reviewRequestId).orElseThrow(InvalidParameterException::new);
    }
}
