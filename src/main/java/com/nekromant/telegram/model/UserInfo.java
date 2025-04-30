package com.nekromant.telegram.model;

import com.nekromant.telegram.contants.UserType;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserInfo {
    @Id
    private Long chatId;

    @Column
    private String userName;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column
    private Boolean notifyAboutReports;

    @Column
    private String timezone;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(chatId, userInfo.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatId);
    }
}
