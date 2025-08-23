package com.nekromant.telegram.commands.notification;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.service.NotificationReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.NOTIFY_REPORT_OFF;

@Slf4j
@Component
public class DisableReportNotification extends MentoringReviewCommand {

    @Autowired
    private NotificationReportService notificationReportService;

    @Value("${owner.userName}")
    private String ownerUserName;

    public DisableReportNotification() {
        super(NOTIFY_REPORT_OFF.getAlias(), NOTIFY_REPORT_OFF.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        log.info("Выключение уведомлений о пользователях, ненаписавших отчет");
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        if (!user.getUserName().equals(ownerUserName)) {
            message.setText("Ты не владелец бота");
            execute(absSender, message, user);
            return;
        }

        try {
            notificationReportService.disableReportNotification();
            message.setText("Уведомления о пользователях, ненаписавших отчет отключены");
            execute(absSender, message, user);
        } catch (Exception e) {
            message.setText("Что-то пошло не так " + e.getMessage());
            execute(absSender, message, user);
        }


    }
}
