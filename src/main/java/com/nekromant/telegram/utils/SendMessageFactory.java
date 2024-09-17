package com.nekromant.telegram.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class SendMessageFactory {
    public SendMessage create(String chatId, String text) {
        return new SendMessage(chatId, text);
    }
}
