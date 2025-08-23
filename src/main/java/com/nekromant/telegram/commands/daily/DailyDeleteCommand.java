package com.nekromant.telegram.commands.daily;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.DailyService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.DAILY_DELETE;
import static com.nekromant.telegram.contants.MessageContants.*;

@Slf4j
@Component
public class DailyDeleteCommand extends OwnerCommand {
    @Autowired
    private DailyService dailyService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public DailyDeleteCommand() {
        super(DAILY_DELETE.getAlias(), DAILY_DELETE.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            SendMessage message = sendMessageFactory.create(chat);
            dailyService.deleteDailyByChatId(chat.getId().toString());
            message.setText(DAILY_DELETED);
            execute(absSender, message, user);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR);
            execute(absSender, message, user);
        }
    }

}
