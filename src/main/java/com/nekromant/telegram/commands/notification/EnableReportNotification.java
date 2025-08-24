package com.nekromant.telegram.commands.notification;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.NotificationReportService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.NOTIFY_REPORT_ON;


@Slf4j
@Component
public class EnableReportNotification extends OwnerCommand {
    @Autowired
    private NotificationReportService notificationReportService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public EnableReportNotification() {
        super(NOTIFY_REPORT_ON.getAlias(), NOTIFY_REPORT_ON.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        log.info("Включение уведомлений о пользователях, ненаписавших отчет");
        SendMessage message = sendMessageFactory.create(chat);

        try {
            notificationReportService.enableReportNotification();
            message.setText("Уведомления о пользователях, ненаписавших отчет включены");
            execute(absSender, message, user);
        } catch (Exception e) {
            message.setText("Что-то пошло не так " + e.getMessage());
            execute(absSender, message, user);
        }
    }
}

