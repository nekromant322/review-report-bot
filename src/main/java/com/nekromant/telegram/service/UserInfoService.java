package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private MentorRepository mentorRepository;

    public void initializeUserInfo(Chat chat, User user) {
        log.info("Инициализация нового пользователя: {}", user.getUserName());
        if (isPrivateChat(chat)
                && isNewUserOrUserWithoutName(user)) {
            log.info("Инициализация пользователя {} идёт в приватном чате, пользователя не существует или его имя не существует в БД", user.getUserName());
            UserInfo userInfo = UserInfo.builder()
                    .userName(user.getUserName())
                    .chatId(user.getId())
                    .userType(UserType.STUDENT)
                    .build();
            userInfoRepository.save(userInfo);
            log.info("Был создан новый пользователь: {}", userInfo.getUserName());
        }
    }

    private boolean isNewUserOrUserWithoutName(User user) {
        UserInfo userInfoByChatId = userInfoRepository.findUserInfoByChatId(user.getId());
        log.info("userInfoByChatId: {}", userInfoByChatId);
        UserInfo userInfoByUserName = userInfoRepository.findUserInfoByUserName(user.getUserName());
        log.info("userInfoByUserName: {}", userInfoByUserName);
        return userInfoByChatId == null || userInfoByUserName == null;
    }

    private static boolean isPrivateChat(Chat chat) {
        return chat.getType().equalsIgnoreCase("private");
    }

    public void promoteUserToMentor(String userName) {
        UserInfo userInfo = getUserInfo(userName);
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

    public boolean updateTimezone(Long chatId, String timezone) {
        UserInfo userInfo = userInfoRepository.findUserInfoByChatId(chatId);

        if (timezone != null) {
            userInfo.setTimezone(timezone);
            userInfoRepository.save(userInfo);
            return true;
        }

        return false;
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

    public List<UserInfo> findAllMentors() {
        List<Mentor> allMentors = mentorRepository.findAll();
        return allMentors.stream().map(Mentor::getMentorInfo).collect(Collectors.toList());
    }
}
