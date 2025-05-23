package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.NotificationPay;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface NotificationPayRepository extends JpaRepository<NotificationPay, Long> {
    NotificationPay getNotificationPayByUserInfo(UserInfo userInfo);
    List<NotificationPay> getNotificationPayByEnable(Boolean type);
}
