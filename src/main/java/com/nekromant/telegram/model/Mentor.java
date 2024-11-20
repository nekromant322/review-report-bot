package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Mentor {
    @Id
    private Long mentorInfoChatId;

    @MapsId
    @OneToOne
    private UserInfo mentorInfo;

    @Column
    private Boolean isActive = true;

    @Column
    private String roomUrl;
}
