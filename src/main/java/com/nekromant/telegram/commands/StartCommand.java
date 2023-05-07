package com.nekromant.telegram.commands;

import com.nekromant.telegram.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contents.Command.START;
import static com.nekromant.telegram.contents.MessageContents.START_HELP_MESSAGE;

@Component
public class StartCommand extends MentoringReviewCommand {

    @Autowired
    private UserInfoService userInfoService;

    public StartCommand() {
        super(START.getAlias(), START.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

        userInfoService.initializeUserInfo(chat, user);

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(START_HELP_MESSAGE);

        //если хочется присобачить клавиатуру - это сюда
        message.setReplyMarkup(new ReplyKeyboardRemove(true));

        execute(absSender, message, user);
    }
}
