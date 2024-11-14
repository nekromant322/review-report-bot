package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MentorService {
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private UserInfoService userInfoService;

    public void saveMentor(String newMentorUserName, String newMentorRoom) {
        UserInfo mentorInfo = userInfoService.getUserInfo(newMentorUserName);
        Mentor mentor = Mentor.builder()
                .mentorInfo(mentorInfo)
                .mentorInfoChatId(mentorInfo.getChatId())
                .isActive(true)
                .roomUrl(newMentorRoom)
                .build();
        mentorRepository.save(mentor);
    }
}
