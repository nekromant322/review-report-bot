package com.nekromant.telegram;

import org.springframework.stereotype.Service;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;


import java.io.InputStream;

/**
 * Contains various methods for building different type of telegram messages, such as:
 * text message, pop-up notification, edited text message, text message with image, document.
 *
 * Markdown feature should be disabled to prevent unexpected errors: information about requested game may contains
 * symbols that probably cannot be parsed. This case can throws unexpected {@link org.telegram.telegrambots.meta.exceptions.TelegramApiException}.
 */
@Service
public class ReplyMessageService {

    public SendMessage getTextMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        return sendMessage;
    }

    /**
     * Uses as answers to callback queries {@link org.telegram.telegrambots.meta.api.objects.CallbackQuery}.
     */
    public AnswerCallbackQuery getPopUpAnswer(String callbackId, String text) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackId);
        answerCallbackQuery.setText(text);
        answerCallbackQuery.setShowAlert(false);
        return answerCallbackQuery;
    }

    /**
     * Uses to hide keyboard {@link org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup}.
     */
    public EditMessageText getEditedTextMessage(Long chatId, Integer messageId, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        return editMessageText;
    }


}

