package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contents.Command.NOTIFY_REVIEW_ON;
import static com.nekromant.telegram.contents.MessageContents.SUBSCRIBED_ON_NOTIFICATIONS;

@Component
public class NotifyOnCommand extends MentoringReviewCommand {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public NotifyOnCommand() {
        super(NOTIFY_REVIEW_ON.getAlias(), NOTIFY_REVIEW_ON.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        try {

            UserInfo userInfo = userInfoService.getUserInfo(user.getUserName());
            userInfo.setNotifyAboutReports(true);
            userInfoService.save(userInfo);

        } catch (Exception e) {
            message.setText("Что-то пошло не так" + e.getMessage());
            execute(absSender, message, user);
        }

        message.setText(SUBSCRIBED_ON_NOTIFICATIONS);
        execute(absSender, message, user);
    }
}
