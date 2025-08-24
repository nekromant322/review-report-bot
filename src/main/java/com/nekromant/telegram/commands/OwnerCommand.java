package com.nekromant.telegram.commands;

import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

@Slf4j
public abstract class OwnerCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public OwnerCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        log.info("Проверка пользователя на владельца бота");
        SendMessage message = sendMessageFactory.create(chat);
        if (!user.getUserName().equals(ownerUserName)) {
            log.info(NOT_OWNER_ERROR);
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        log.info("Пользователь владелец, вызов " + this.getCommandIdentifier());
        executeOwner(absSender, user, chat, arguments);
    }

    protected abstract void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments);
}
