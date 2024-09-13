package com.nekromant.telegram.commands.daily;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.service.DailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.DAILY_DELETE;
import static com.nekromant.telegram.contants.MessageContants.*;

@Slf4j
@Component
public class DailyDeleteCommand extends MentoringReviewCommand {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private DailyService dailyService;

    public DailyDeleteCommand() {
        super(DAILY_DELETE.getAlias(), DAILY_DELETE.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            SendMessage message = new SendMessage();
            String chatId = chat.getId().toString();
            message.setChatId(chatId);
            if (!user.getUserName().equals(ownerUserName)) {
                message.setText(NOT_OWNER_ERROR);
                execute(absSender, message, user);
                return;
            }
            dailyService.deleteDailyByChatId(chatId);
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
