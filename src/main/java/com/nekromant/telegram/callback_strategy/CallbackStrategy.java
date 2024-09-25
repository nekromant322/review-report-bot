package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.contants.CallBack;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CallbackStrategy {
    void executeCallbackQuery(Update update, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) throws TelegramApiException;
    default CallBack getPrefix() {
        String className = getClass().getSimpleName();
        String strategyPrefix = className.substring(0, className.indexOf("Callback"));
        return CallBack.from(strategyPrefix);
    }
}
