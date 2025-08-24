package com.nekromant.telegram.commands.daily;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.service.DailyService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class DailyCommand extends OwnerCommand {
    @Autowired
    private DailyService dailyService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public DailyCommand() {
        super(DAILY.getAlias(), DAILY.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            SendMessage message = sendMessageFactory.create(chat);
            validateArgumentsNumber(strings);
            Daily daily = new Daily();
            daily.setChatId(chat.getId().toString());
            daily.setTime(LocalTime.parse(strings[0]));
            daily.setMessage(parseMessage(strings));
            dailyService.saveDaily(daily);
            message.setText(DAILY_CREATED);
            execute(absSender, message, user);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
