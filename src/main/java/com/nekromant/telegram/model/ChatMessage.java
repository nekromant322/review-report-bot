package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
    @NotNull
    private Integer userMessageId;
    private Integer userChatBotMessageId;
    @OneToOne
    private Report report;
    private Integer reportChatBotMessageId;
}
