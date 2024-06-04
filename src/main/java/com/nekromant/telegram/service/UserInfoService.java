package com.nekromant.telegram.service;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.Role;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Value("${owner.userName}")
    private String ownerUserName;

    @Value("${owner.password}")
    private String ownerPassword;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void initializeUserInfo(Chat chat, User user) {
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

    public void save(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }

    public List<UserInfo> getAllUsersReportNotificationsEnabled() {
        return userInfoRepository.findAllByNotifyAboutReportsIsTrue();
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Role adminRole = new Role();
        adminRole.setTitle("ROLE_admin");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        return UserInfo.builder().
                userName(ownerUserName).
                password(passwordEncoder.encode(ownerPassword)).
                roles(roles).build();
    }
}
