package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Service
public class UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private MentorRepository mentorRepository;

    public List<UserInfo> findAllByUserType(UserType userType) {
        return userInfoRepository.findAllByUserType(userType);
    }

    public void initializeUserInfo(Chat chat, User user) {
        if (chat.getType().equalsIgnoreCase("private")
                && (userInfoRepository.findUserInfoByChatId(user.getId()) == null || userInfoRepository.findUserInfoByUserName(user.getUserName()) == null)) {
            UserInfo userInfo = UserInfo.builder()
                    .userName(user.getUserName())
                    .chatId(user.getId())
                    .userType(UserType.STUDENT)
                    .build();
            userInfoRepository.save(userInfo);
        }
    }

    public void promoteUserToMentor(UserInfo userInfo) {
        if (userInfo.getUserType() != UserType.MENTOR) {
            userInfo.setUserType(UserType.MENTOR);
            userInfoRepository.save(userInfo);
        }
    }

    public void demoteMentorToDev(Long userChatId) {
        UserInfo userInfo = userInfoRepository.findUserInfoByChatId(userChatId);
        if (userInfo.getUserType() == UserType.MENTOR) {
            Mentor deleteMentor = mentorRepository.findMentorByMentorInfo(userInfo);
            mentorRepository.delete(deleteMentor);
            userInfo.setUserType(UserType.DEV);
            userInfoRepository.save(userInfo);
        }
    }

    public UserInfo getUserInfo(String userName) {
        return userInfoRepository.findUserInfoByUserName(userName);
    }

    public UserInfo getUserInfo(Long userId) {
        return userInfoRepository.findUserInfoByChatId(userId);
    }

    public void save(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }

    public List<UserInfo> getAllUsersReportNotificationsEnabled() {
        return userInfoRepository.findAllByNotifyAboutReportsIsTrue();
    }
}
