package com.nekromant.telegram;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.commands.MentoringReviewWithMessageIdCommand;
import com.nekromant.telegram.service.update_handler.NonCommandUpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Component
@Slf4j
public class MentoringReviewBot extends TelegramLongPollingCommandBot {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    @Lazy
    @Autowired
    private NonCommandUpdateHandler nonCommandUpdateHandler;

    @Autowired
    public MentoringReviewBot(List<MentoringReviewCommand> allCommands,
                              List<MentoringReviewWithMessageIdCommand> allWithMessageIdCommands) {
        super();
        allCommands.forEach(this::register);
        allWithMessageIdCommands.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        nonCommandUpdateHandler.handleUpdate(update);
    }
}