package com.nekromant.telegram.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SpecialChats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String mentorsChatId;

    @Column
    private String reportChatId;

    public SpecialChats(String mentorsChatId) {
        this.mentorsChatId = mentorsChatId;
    }
}
