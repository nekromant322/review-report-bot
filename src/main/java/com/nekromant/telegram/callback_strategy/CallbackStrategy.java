package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public interface CallbackStrategy {
    void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) throws TelegramApiException;
    CallBack getPrefix();
}
