package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Представляет собой связь между сообщением пользователя, отчётом в БД и сообщениями бота в чате пользователя и чате отчётов
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * ID сообщения пользователя, в котором была вызвана команда.
     */
    @NotNull
    private Integer userMessageId;
    /**
     * ID сообщения бота в чате юзера с отправленным отчётом.
     */
    private Integer userChatBotMessageId;
    /**
     * ID отчёта в БД, который был закреплён за сообщением.
     */
    @OneToOne
    private Report report;
    /**
     * ID сообщения бота в чате отчётов с отправленным отчётом.
     */
    private Integer reportChatBotMessageId;
}
