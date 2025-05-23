package com.nekromant.telegram.commands.notification;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.MessageContants;
import com.nekromant.telegram.service.NotificationService;
import com.nekromant.telegram.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;

import static com.nekromant.telegram.contants.Command.DISABLE_NOTIFICATION;

@Slf4j
@Component
public class DisablePayNotification extends MentoringReviewCommand {

    @Autowired
    private NotificationService notificationService;

    @Value("${owner.userName}")
    private String ownerUserName;

    public DisablePayNotification() {
        super(DISABLE_NOTIFICATION.getAlias(), DISABLE_NOTIFICATION.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.info("Выключение уведомлений об оплате подписки для пользователей");
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        ValidationUtils.validateArgumentsNumber(strings);

        try {
            if (!chat.getUserName().equals(ownerUserName)) {
                message.setText("Ты не владелец бота");
                execute(absSender, message, user);
            } else {
                notificationService.disablePayNotification(strings);
                message.setText(MessageContants.SUCCESS_DISABLE_NOTIFICATION);
                execute(absSender, message, user);
            }
        } catch (InvalidParameterException e) {
            message.setText("Укажи аргументы");
            execute(absSender, message, user);
        } catch (Exception e) {
            message.setText(MessageContants.FAILED_DISABLE_NOTIFICATION);
            execute(absSender, message, user);
        }
    }
}
