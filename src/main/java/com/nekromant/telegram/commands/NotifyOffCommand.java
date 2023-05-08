package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.NOTIFY_REVIEW_OFF;
import static com.nekromant.telegram.contants.MessageContants.SUBSCRIBED_OFF_NOTIFICATIONS;

@Component
public class NotifyOffCommand extends MentoringReviewCommand {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public NotifyOffCommand() {
        super(NOTIFY_REVIEW_OFF.getAlias(), NOTIFY_REVIEW_OFF.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        try {

            UserInfo userInfo = userInfoService.getUserInfo(user.getUserName());
            userInfo.setNotifyAboutReports(false);
            userInfoService.save(userInfo);

        } catch (Exception e) {
            message.setText("Что-то пошло не так" + e.getMessage());
            execute(absSender, message, user);
        }

        message.setText(SUBSCRIBED_OFF_NOTIFICATIONS);
        execute(absSender, message, user);
    }
}
