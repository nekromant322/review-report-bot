package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Slf4j
@Component
public class DenyReviewRequestDateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Override
    public CallBack getPrefix() {
        return CallBack.DENY_REVIEW_REQUEST_DATE_TIME;
    }

    @Override
    public void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);

        String callbackData = update.getCallbackQuery().getData();
        Long reviewRequestId = Long.parseLong(callbackData.split(" ")[1]);

        setMessageForUser(messageForUser);
        deleteReviewRequest(reviewRequestId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    private void deleteReviewRequest(Long reviewRequestId) {
        reviewRequestRepository.findById(reviewRequestId).ifPresent(report -> reviewRequestRepository.deleteById(reviewRequestId));
    }

    private void setMessageForUser(SendMessage messageForUser) {
        messageForUser.setText("Отправка запроса на ревью отменена");
    }
}
