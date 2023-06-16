package com.nekromant.telegram.commands.daily;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.service.DailyService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalTime;

import static com.nekromant.telegram.contants.Command.DAILY;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

public class DailyCommand extends MentoringReviewCommand {
    @Autowired
    private DailyService dailyService;
    @Value("${owner.userName}")
    private String ownerUserName;

    public DailyCommand() {
        super(DAILY.getAlias(), DAILY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        try {
            ValidationUtils.validateArguments(strings);
            Daily daily = new Daily();
            daily.setChatId(chatId);
            daily.setTime(LocalTime.parse(strings[0]));
            daily.setMessage(strings[1]);
            dailyService.saveDaily(daily);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
