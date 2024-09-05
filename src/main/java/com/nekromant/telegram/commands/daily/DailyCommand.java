package com.nekromant.telegram.commands.daily;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.service.DailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.DAILY;
import static com.nekromant.telegram.contants.MessageContants.*;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Component
public class DailyCommand extends MentoringReviewCommand {
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private DailyService dailyService;

    public DailyCommand() {
        super(DAILY.getAlias(), DAILY.getDescription());
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
            validateArgumentsNumber(strings);
            Daily daily = new Daily();
            daily.setChatId(chatId);
            daily.setTime(LocalTime.parse(strings[0]));
            daily.setMessage(parseMessage(strings));
            dailyService.saveDaily(daily);
            message.setText(DAILY_CREATED);
            execute(absSender, message, user);

        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR + DAILY_HELP_MESSAGE);
            execute(absSender, message, user);
        }
    }

    private String parseMessage(String[] strings) {
        return Arrays.stream(strings).skip(1).collect(Collectors.joining(" "));
    }

}
