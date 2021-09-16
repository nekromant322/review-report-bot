package com.nekromant.telegram.model;

import com.nekromant.telegram.contants.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserInfo {

    @Id
    private String userName;

    @Column
    private Long chatId;

    @Enumerated(EnumType.STRING)
    private UserType userType;
}
