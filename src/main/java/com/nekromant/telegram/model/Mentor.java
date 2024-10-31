package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Mentor {
    @Id
    private Long mentorInfoChatId;

    @MapsId
    @JoinColumn(name = "mentor_info_chat_id")
    @ManyToOne
    private UserInfo mentorInfo;

    @Column
    private Boolean isActive = true;

    @Column
    private String roomUrl;
}
