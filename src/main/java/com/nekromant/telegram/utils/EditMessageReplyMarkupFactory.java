package com.nekromant.telegram.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class EditMessageReplyMarkupFactory {

    public EditMessageReplyMarkup createFromCallbackMessage(Message callbackMessage) {
        return create(callbackMessage.getChatId().toString(), callbackMessage.getMessageId());
    }

    public EditMessageReplyMarkup create(String chatId, Integer messageId) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(null);
        return editMessageReplyMarkup;
    }
}
