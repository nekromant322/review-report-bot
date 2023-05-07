package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nekromant.telegram.contants.Command.ANNOUNCE;
import static com.nekromant.telegram.contants.MessageContants.*;

@Component
public class AnnounceCommand extends MentoringReviewCommand {
    //TODO поменять назад владельца
    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public AnnounceCommand() {
        super(ANNOUNCE.getAlias(), ANNOUNCE.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        StringBuilder newAnnounce = new StringBuilder();
        StringBuilder announceRecipients = new StringBuilder();
        try {
            ValidationUtils.validateArguments(arguments);
            List<String> newAnnounceList = new ArrayList<>();
            List<String> announceRecipientsList = new ArrayList<>();
            for (String argument : arguments) {
                if (argument.contains("@")) {
                    announceRecipientsList.add(argument.replaceAll("\\p{Punct}", ""));
                } else {
                    newAnnounceList.add(argument.replace("\"", ""));
                }
            }
            newAnnounceList.forEach(word -> {
                newAnnounce.append(word);
                newAnnounce.append(" ");
            });

            announceRecipientsList.forEach(recipient -> {
                try {
                    UserInfo recipientInfo = userInfoService.getUserInfo(recipient.replace("@", ""));
                    if (Objects.equals(recipientInfo, null)) {
                        throw new Exception("User not found");
                    }
                    User recipientUser = new User(recipientInfo.getChatId(), recipientInfo.getUserName(), false);
                    SendMessage messageForRecipient = new SendMessage();
                    messageForRecipient.setChatId(recipientInfo.getChatId().toString());
                    messageForRecipient.setText(newAnnounce.toString());
                    execute(absSender, messageForRecipient, recipientUser, true);
                    announceRecipients.append(recipient);
                    announceRecipients.append(" ");
                } catch (Exception e) {
                    message.setText(e.getMessage() + "\n" + "Что-то пошло не так с @" + recipient);
                    execute(absSender, message, user);
                }
            });
            message.setText(ANNOUNCE_SENT);
            execute(absSender, message, user, true);
        } catch (Exception e) {
            message.setText(e.getClass() + "\n" + "Пример: /announce \"Текст аннонса\" @UserName");
            execute(absSender, message, user);
        }
    }
}
