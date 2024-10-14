package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class SendObject {
    private ChatType chatType;
    private SendMessage sendMessage;
    private ChatMessage chatMessage;

    public void setMessageIdByChatType(Integer messageId) {
        if (chatType == ChatType.REPORTS_CHAT) {
            chatMessage.setReportChatBotMessageId(messageId);
        } else if (chatType == ChatType.USER_CHAT) {
            chatMessage.setUserChatBotMessageId(messageId);
        }
    }

    public Integer getMessageIdByChatType() {
        if (chatType == ChatType.REPORTS_CHAT) {
            return chatMessage.getReportChatBotMessageId();
        } else {
            if (chatType != ChatType.USER_CHAT) {
                log.error("Был передан неподдерживаемый тип чата при обновлении текста сообщения: {}", chatType);
            }
            return chatMessage.getUserChatBotMessageId();
        }
    }
}
