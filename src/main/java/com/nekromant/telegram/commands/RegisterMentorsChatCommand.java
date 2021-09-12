package com.nekromant.telegram.commands;

import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REGISTER_MENTOR;
import static com.nekromant.telegram.contants.MessageContants.NOW_OWNER_ERROR;

@Component
public class RegisterMentorsChatCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    public RegisterMentorsChatCommand() {
        super(REGISTER_MENTOR.getAlias(), REGISTER_MENTOR.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOW_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        try {
            specialChatService.updateMentorsChatId(chatId);
        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Этот чат теперь основной чат менторов, сюда будут приходить запросы о ревью");
        execute(absSender, message, user);
    }
}

