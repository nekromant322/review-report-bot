package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.callback_strategy.utils.StrategyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class DateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private StrategyUtils strategyUtils;
    @Override
    public void executeCallbackQuery(Update update, String callbackData, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        strategyUtils.setReportDate(update, callbackData, messageForUser, messageForReportsChat, deleteMessageStrategy);
    }
}
