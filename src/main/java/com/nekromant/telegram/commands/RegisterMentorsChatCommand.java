package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.MentorsChat;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.MentorsChatRepository;
import com.nekromant.telegram.service.MentorsChatService;
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
public class RegisterMentorsChatCommand extends MentoringReviewCommand {

    @Value("${owner.userName")
    private String ownerUserName;

    @Autowired
    private MentorsChatService mentorsChatService;

    @Autowired
    public RegisterMentorsChatCommand() {
        super("register", "Изменить список менторов");
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
            mentorsChatService.updateMentorsChatId(chatId);


        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Этот чат теперь основной чат менторов, сюда будут приходить запросы о ревью");
        execute(absSender, message, user);
    }

    private Set<String> parseMentorsUserNames(String[] arguments) {
        return Arrays.stream(arguments).map(x -> x.replaceAll("@", "")).collect(Collectors.toSet());
    }

    private void validateArguments(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException();
        }
    }

}

