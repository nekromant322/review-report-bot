package com.nekromant.telegram.commands.review;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.SchedulePeriodService;
import com.nekromant.telegram.utils.SendMessageFactory;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.SET_SCHEDULE_PERIOD;
import static com.nekromant.telegram.contants.MessageContants.PERIOD_IS_SET;

@Component
public class SetSchedulePeriodCommand extends OwnerCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private SchedulePeriodService schedulePeriodService;

    @Autowired
    public SetSchedulePeriodCommand() {
        super(SET_SCHEDULE_PERIOD.getAlias(), SET_SCHEDULE_PERIOD.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            schedulePeriodService.setStart(Long.valueOf(arguments[0]));
            schedulePeriodService.setEnd(Long.valueOf(arguments[1]));

        } catch (Exception e) {
            message.setText(e.getMessage() + "\n" + "Пример: /set_period 18 3");
            execute(absSender, message, user);
        }

        message.setText(PERIOD_IS_SET);
        execute(absSender, message, user);
    }
}
