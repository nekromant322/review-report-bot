package com.nekromant.telegram.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class DeleteMessageFactory {

    public DeleteMessage createFromCallbackMessage(Message callbackMessage) {
        return create(callbackMessage.getChatId().toString(), callbackMessage.getMessageId());
    }

    public DeleteMessage create(String chatId, Integer messageId) {
        return new DeleteMessage(chatId, messageId);
    }
}
