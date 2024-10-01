package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    ChatMessage findByUserMessageId(Integer userMessageId);
    ChatMessage findChatMessageByReport(Report report);
    void deleteChatMessageByReport(Report report);
}
