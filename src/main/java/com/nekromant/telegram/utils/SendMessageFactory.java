package com.nekromant.telegram.utils;

import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SendMessageFactory {
    @Autowired
    private SpecialChatService specialChatService;

    public SendMessage create(String chatId, String text) {
        return new SendMessage(chatId, text);
    }

    public SendMessage createFromUpdate(Update update, ChatType chatType) {
        SendMessage sendMessage = new SendMessage();
        setChatIdForSendMessage(update, sendMessage, chatType);
        return sendMessage;
    }

    private void setChatIdForSendMessage(Update update, SendMessage sendMessage, ChatType chatType) {
        switch (chatType) {
            case USER_CHAT:
                setChatIdForUser(update, sendMessage);
                break;
            case MENTORS_CHAT:
                setChatIdForMentors(sendMessage);
                break;
            case REPORTS_CHAT:
                setChatIdForReportsChat(sendMessage);
                break;
        }
    }

    private void setChatIdForUser(Update update, SendMessage messageForUser) {
        messageForUser.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
    }

    private void setChatIdForMentors(SendMessage messageForMentors) {
        messageForMentors.setChatId(specialChatService.getMentorsChatId());
    }

    private void setChatIdForReportsChat(SendMessage messageForReportsChat) {
        messageForReportsChat.setChatId(specialChatService.getReportsChatId());
    }
}
