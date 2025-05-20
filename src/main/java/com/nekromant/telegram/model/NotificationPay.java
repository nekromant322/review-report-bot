package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class NotificationPay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chatId")
    private UserInfo userInfo;

    private boolean enable;
}
