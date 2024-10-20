package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Service
public class UnsupportedBehaviourHandler {

    @Autowired
    private SendMessageFactory sendMessageFactory;

    public <T extends Serializable, Method extends BotApiMethod<T>> List<Method> handleError(Update update, String errorMessage) {
        String chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        } if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
        } else if (update.hasEditedMessage()) {
            chatId = update.getEditedMessage().getChatId().toString();
        } else {
            return null;
        }

        if (chatId == null) {
            return null;
        } else {
            SendMessage sendMessage = sendMessageFactory.create(chatId, errorMessage);
            return List.of((Method) sendMessage);
        }
    }
}
