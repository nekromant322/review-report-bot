package com.nekromant.telegram.service;


import com.nekromant.telegram.model.MentorsChat;
import com.nekromant.telegram.repository.MentorsChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MentorsChatService {

    private MentorsChatRepository mentorsChatRepository;

    private String cachedMentorsChatId = "";

    @Autowired
    public MentorsChatService(MentorsChatRepository mentorsChatRepository) {
        this.mentorsChatRepository = mentorsChatRepository;
        List<MentorsChat> mentorsChats = mentorsChatRepository.findAll();
        if(!mentorsChats.isEmpty()) {
            this.cachedMentorsChatId = mentorsChats.get(0).getChatId();
        }
    }

    public void updateMentorsChatId(String mentorsChatId) {
        List<MentorsChat> mentorsChats = mentorsChatRepository.findAll();
        if(mentorsChats.isEmpty()) {
            mentorsChatRepository.save(new MentorsChat(mentorsChatId));
        } else {
            mentorsChatRepository.deleteAll();
            mentorsChatRepository.save(new MentorsChat(mentorsChatId));
        }
        cachedMentorsChatId = mentorsChatId;
    }

    public String getMentorsChatId() {
        return this.cachedMentorsChatId;
    }
}
