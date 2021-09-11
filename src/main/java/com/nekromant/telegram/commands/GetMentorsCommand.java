package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.GET_MENTORS;

@Component
public class GetMentorsCommand extends MentoringReviewCommand {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public GetMentorsCommand() {
        super(GET_MENTORS.getAlias(), GET_MENTORS.getDescription());
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
                        .collect(Collectors.joining("\n")));
        execute(absSender, message, user);
    }
}
