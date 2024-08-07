package com.nekromant.telegram.commands;


import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class MentoringReviewCommand extends BotCommand {

    public MentoringReviewCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    public void execute(AbsSender sender, SendMessage message, User user) {
        log.debug(this.getDescription() + " , пользователь - " + user.getUserName());
        log.debug("output: \n" + message.getText() + "\n");
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}

