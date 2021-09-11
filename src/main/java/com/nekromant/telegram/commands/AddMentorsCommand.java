package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.ADD_MENTORS;

@Component
public class AddMentorsCommand extends MentoringReviewCommand {

    @Value("${owner.userName")
    private String ownerUserName;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public AddMentorsCommand() {
        super(ADD_MENTORS.getAlias(), ADD_MENTORS.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if(!user.getUserName().equals(ownerUserName)) {
            message.setText("Ты не владелец бота");
        }
        try {
            ValidationUtils.validateArguments(arguments);
            Set<String> mentors = parseMentorsUserNames(arguments);
            mentors.stream().map(Mentor::new).forEach(m -> mentorRepository.save(m));

        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Список менторов изменен");
        execute(absSender, message, user);
    }

    private Set<String> parseMentorsUserNames(String[] arguments) {
        return Arrays.stream(arguments).map(x -> x.replaceAll("@", "")).collect(Collectors.toSet());
    }

}
