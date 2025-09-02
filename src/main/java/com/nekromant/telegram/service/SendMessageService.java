package com.nekromant.telegram.service;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Slf4j
@Service
public class SendMessageService {

    @Autowired
    private MentoringReviewBot mentoringReviewBot;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public Message sendMessage(SendMessage sendMessage) throws TelegramApiException {
        return mentoringReviewBot.execute(sendMessage);
    }

    public Serializable sendMessage(EditMessageText editMessageText) throws TelegramApiException {
        return mentoringReviewBot.execute(editMessageText);
    }

    public Boolean sendMessage(DeleteMessage deleteMessage) throws TelegramApiException {
        return mentoringReviewBot.execute(deleteMessage);
    }

    public Serializable sendMessage(EditMessageReplyMarkup editMessageReplyMarkup) throws TelegramApiException {
        return mentoringReviewBot.execute(editMessageReplyMarkup);
    }

    public void sendMessage(String chatId, String text) {
        SendMessage sendMessage = sendMessageFactory.create(chatId, text);
        sendMessage.disableWebPagePreview();
        try {
            mentoringReviewBot.execute(sendMessage);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения (chat id: {}) {}", chatId, e.getMessage(), e);
        }
    }
}
