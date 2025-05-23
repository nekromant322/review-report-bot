package com.nekromant.telegram.service;

import com.nekromant.telegram.model.NotificationPay;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.NotificationPayRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationPayRepository notificationPayRepository;
    private final UserInfoRepository userInfoRepository;

    public void enablePayNotification(String[] strings) {
        List<UserInfo> userInfoList = getUsers(extractUsernames(strings));

        List<NotificationPay> notificationPayList = new ArrayList<>();
        for (UserInfo user : userInfoList) {
            NotificationPay pay = getNotification(user);
            pay.setEnable(true);
            notificationPayList.add(pay);
        }
        saveNotificationChanges(notificationPayList);
    }

    public void disablePayNotification(String[] strings) {
        List<UserInfo> userInfoList = getUsers(extractUsernames(strings));

        List<NotificationPay> notificationPayList = new ArrayList<>();
        for (UserInfo user : userInfoList) {
            NotificationPay pay = getNotification(user);
            pay.setEnable(false);
            notificationPayList.add(pay);
        }
        saveNotificationChanges(notificationPayList);
    }

    public NotificationPay getNotification(UserInfo userInfo) {
        NotificationPay notificationPay;

        try {
            notificationPay = notificationPayRepository.getNotificationPayByUserInfo(userInfo);
            if (notificationPay == null) {
                notificationPay = NotificationPay.builder()
                        .userInfo(userInfo)
                        .build();
            }
        } catch (Exception e) {
            notificationPay = NotificationPay.builder()
                    .userInfo(userInfo)
                    .build();
        }

        return notificationPay;
    }

    private void saveNotificationChanges(List<NotificationPay> notificationPayList) {
        notificationPayRepository.saveAll(notificationPayList);
    }

    public List<String> extractUsernames(String[] strings) {
        return Arrays.stream(strings)
                .filter(part -> part.startsWith("@") && part.length() > 1)
                .map(part -> part.substring(1))
                .collect(Collectors.toList());
    }

    public List<UserInfo> getUsers(List<String> users) {
        List<UserInfo> userInfoList = new ArrayList<>();
        for (String username : users) {
            userInfoList.add(userInfoRepository.findUserInfoByUserName(username));
        }

        return userInfoList;
    }
}
