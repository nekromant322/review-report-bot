package com.nekromant.telegram.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.START;
import static com.nekromant.telegram.contants.MessageContants.START_MESSAGE;

@Component
public class StartCommand extends MentoringReviewCommand {

    public StartCommand() {
        super(START.getAlias(), START.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        SendMessage message = new SendMessage();
//        message.setReplyMarkup(replyKeyboardMarkup);
//        List<KeyboardRow> keyboardRows = new ArrayList<>();
//        List<String> arrayList = new ArrayList<>();
//
//        for (int i = 0; i < arrayList.size(); i++) {
//            KeyboardRow keyboardRow = new KeyboardRow();
//            keyboardRow.add(arrayList.get(i));
//            keyboardRows.add(keyboardRow);
//        }
//        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(false);
//        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setChatId(chat.getId().toString());
        message.setText(START_MESSAGE);
        execute(absSender, message, user);
    }



}
