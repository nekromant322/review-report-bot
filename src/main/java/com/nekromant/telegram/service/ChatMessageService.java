package com.nekromant.telegram.service;

import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage findChatMessageByUserMessageId(Integer userMessageId) {
        return chatMessageRepository.findByUserMessageId(userMessageId);
    }

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public void deleteByReport(Report report) {
        chatMessageRepository.deleteChatMessageByReport(report);
    }
}
