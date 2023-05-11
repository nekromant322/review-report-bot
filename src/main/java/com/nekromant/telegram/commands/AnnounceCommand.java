package com.nekromant.telegram.commands;

import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nekromant.telegram.contants.Command.ANNOUNCE;
import static com.nekromant.telegram.contants.MessageContants.*;

@Component
public class AnnounceCommand extends MentoringReviewCommand {
    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public AnnounceCommand() {
        super(ANNOUNCE.getAlias(), ANNOUNCE.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }
        try {
            ValidationUtils.validateArguments(args);
            String announce = parseAnnounce(args);
            parseRecipients(args).forEach(recipient -> {
                try {
                    sendAnnounce(absSender, announce, recipient);

                } catch (NotFoundException | TelegramApiException e) {
                    message.setText(e.getMessage() + "\n" + "Что-то пошло не так с @" + recipient);
                    execute(absSender, message, user);
                }
            });
        } catch (Exception e) {
            message.setText(e.getClass() + "\n" + ANNOUNCE_HELP_MESSAGE);
            execute(absSender, message, user);
        }
    }

    private String parseAnnounce(String[] args) {
        String inputText = String.join(" ", args);
        if (!inputText.matches("\".*\" @.*")) {
            throw new InvalidParameterException();
        }
        return inputText.substring(inputText.indexOf("\"") + 1, inputText.lastIndexOf("\""));
    }

    private List<String> parseRecipients(String[] args) {
        String inputText = String.join(" ", args);
        List<String> recipientsList = new ArrayList<>();
        for (String argument : inputText.substring(inputText.lastIndexOf("\"") + 1).split(" ")) {
            if (argument.contains("@")) {
                recipientsList.add(argument.replaceAll("@", ""));
            }
        }
        return recipientsList;
    }

    private void sendAnnounce(AbsSender absSender, String announce, String recipient) throws NotFoundException, TelegramApiException {
        UserInfo recipientInfo = userInfoService.getUserInfo(recipient.replace("@", ""));
        if (Objects.equals(recipientInfo, null)) {
            throw new NotFoundException("User not found");
        }
        SendMessage messageForRecipient = new SendMessage();
        messageForRecipient.setChatId(recipientInfo.getChatId().toString());
        messageForRecipient.setText(announce);
        absSender.execute(messageForRecipient);
    }
}
