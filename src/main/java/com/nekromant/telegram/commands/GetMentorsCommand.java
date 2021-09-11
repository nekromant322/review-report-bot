package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetMentorsCommand extends MentoringReviewCommand {

    @Value("${owner.userName")
    private String ownerUserName;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public GetMentorsCommand() {
        super("get_mentors", "Узнать список менторов");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        List<Mentor> activeMentors = new ArrayList<>();
        try {
            activeMentors = mentorRepository.findAllByIsActiveIsTrue();


        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Список активных менторов:\n" +
                activeMentors.stream()
                        .map(Mentor::getUserName)
                        .map(x -> "@" + x)
                        .collect(Collectors.joining("\n ")));
        execute(absSender, message, user);
    }


    private void validateArguments(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException();
        }
    }

}
