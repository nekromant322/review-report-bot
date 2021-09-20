package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private MentorRepository mentorRepository;

    public void updateUserInfo(Chat chat, User user) {
        if (chat.getType().equalsIgnoreCase("private")
                && userInfoRepository.findUserInfoByUserName(user.getUserName()) == null) {
            UserInfo userInfo = UserInfo.builder()
                    .userName(user.getUserName())
                    .chatId(chat.getId())
                    .userType(UserType.STUDENT)
                    .build();
            userInfoRepository.save(userInfo);
        }
    }

    public void promoteUserToMentor(String userName) {
        UserInfo userInfo = userInfoRepository.findUserInfoByUserName(userName);
        if (userInfo.getUserType() != UserType.MENTOR) {
            userInfo.setUserType(UserType.MENTOR);
            userInfoRepository.save(userInfo);
        }
    }

    public List<String> getAllStudentUsernames() {
        return userInfoRepository.findAll().stream()
                .filter(userInfo -> userInfo.getUserType() == UserType.STUDENT)
                .map(UserInfo::getUserName)
                .distinct()
                .collect(Collectors.toList());
    }

    public UserInfo getUserInfo(String userName) {
        return userInfoRepository.findUserInfoByUserName(userName);
    }
}
