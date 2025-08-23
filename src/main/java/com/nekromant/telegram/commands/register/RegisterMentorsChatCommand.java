package com.nekromant.telegram.commands.register;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.utils.SendMessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REGISTER_MENTOR;

@Component
public class RegisterMentorsChatCommand extends OwnerCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    public RegisterMentorsChatCommand() {
        super(REGISTER_MENTOR.getAlias(), REGISTER_MENTOR.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        try {
            specialChatService.updateMentorsChatId(chat.getId().toString());
        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Этот чат теперь основной чат менторов, сюда будут приходить запросы о ревью");
        execute(absSender, message, user);
    }
}

