package com.nekromant.telegram.model;

import com.nekromant.telegram.contants.UserType;
import lombok.*;

import javax.persistence.*;

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

    @Column
    private Boolean notifyAboutReports;
}
