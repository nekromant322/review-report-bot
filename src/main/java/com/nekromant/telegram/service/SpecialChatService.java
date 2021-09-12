package com.nekromant.telegram.service;


import com.nekromant.telegram.model.SpecialChats;
import com.nekromant.telegram.repository.MentorsChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpecialChatService {

    private MentorsChatRepository mentorsChatRepository;


    private String cachedMentorsChatId = "-469295898";

    private String cachedReportsChatId = "-469295898";

    @Autowired
    public SpecialChatService(MentorsChatRepository mentorsChatRepository) {
        this.mentorsChatRepository = mentorsChatRepository;
        List<SpecialChats> specialChats = mentorsChatRepository.findAll();
        if (!specialChats.isEmpty()) {
            this.cachedMentorsChatId = specialChats.get(0).getMentorsChatId();
            this.cachedMentorsChatId = specialChats.get(0).getReportChatId();
        }
    }

    public void updateMentorsChatId(String mentorsChatId) {
        List<SpecialChats> specialChatsList = mentorsChatRepository.findAll();
        if (specialChatsList.isEmpty()) {
            mentorsChatRepository.save(SpecialChats.builder().mentorsChatId(mentorsChatId).build());
        } else {
            SpecialChats specialChats = specialChatsList.get(0);
            specialChats.setMentorsChatId(mentorsChatId);
            mentorsChatRepository.save(specialChats);
        }
        cachedMentorsChatId = mentorsChatId;
    }

    public String getMentorsChatId() {
        return this.cachedMentorsChatId;
    }

    public void updateReportsChatId(String reportsChatId) {
        List<SpecialChats> specialChatsList = mentorsChatRepository.findAll();
        if (specialChatsList.isEmpty()) {
            mentorsChatRepository.save(SpecialChats.builder().reportChatId(reportsChatId).build());
        } else {
            SpecialChats specialChats = specialChatsList.get(0);
            specialChats.setReportChatId(reportsChatId);
            mentorsChatRepository.save(specialChats);
        }
        cachedReportsChatId = reportsChatId;
    }

    public String getReportsChatId() {
        return this.cachedMentorsChatId;
    }
}
