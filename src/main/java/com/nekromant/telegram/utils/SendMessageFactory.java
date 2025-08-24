package com.nekromant.telegram.utils;

import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class SendMessageFactory {
    @Autowired
    private SpecialChatService specialChatService;

    public SendMessage create(String chatId, String text) {
        return new SendMessage(chatId, text);
    }

    public SendMessage create(Chat chat) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        return message;
    }

    public SendMessage createFromCallbackQuery(Message callbackMessage, ChatType chatType) {
        SendMessage sendMessage = new SendMessage();
        setChatIdForSendMessage(callbackMessage, sendMessage, chatType);
        return sendMessage;
    }

    private void setChatIdForSendMessage(Message callbackMessage, SendMessage sendMessage, ChatType chatType) {
        switch (chatType) {
            case USER_CHAT:
                setChatIdForUser(callbackMessage, sendMessage);
                break;
            case MENTORS_CHAT:
                setChatIdForMentors(sendMessage);
                break;
            case REPORTS_CHAT:
                setChatIdForReportsChat(sendMessage);
                break;
        }
    }

    private void setChatIdForUser(Message callbackMessage, SendMessage messageForUser) {
        messageForUser.setChatId(callbackMessage.getChatId().toString());
    }

    private void setChatIdForMentors(SendMessage messageForMentors) {
        messageForMentors.setChatId(specialChatService.getMentorsChatId());
    }

    private void setChatIdForReportsChat(SendMessage messageForReportsChat) {
        messageForReportsChat.setChatId(specialChatService.getReportsChatId());
    }
}
