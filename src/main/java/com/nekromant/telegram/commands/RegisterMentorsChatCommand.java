package com.nekromant.telegram.commands;

import com.nekromant.telegram.service.MentorsChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REGISTER;

@Component
public class RegisterMentorsChatCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private MentorsChatService mentorsChatService;

    @Autowired
    public RegisterMentorsChatCommand() {
        super(REGISTER.getAlias(), REGISTER.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText("Ты не владелец бота");
            execute(absSender, message, user);
            return;
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
}

