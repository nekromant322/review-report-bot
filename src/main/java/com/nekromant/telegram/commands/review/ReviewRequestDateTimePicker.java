package com.nekromant.telegram.commands.review;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.utils.SendMessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class ReviewRequestDateTimePicker {
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public SendMessage sendDatePicker(String userChatId, ReviewRequest reviewRequest) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getDatePickerInlineKeyboardMarkup(reviewRequest);
        SendMessage message = sendMessageFactory.create(userChatId, "Выберите дату");
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    private InlineKeyboardMarkup getDatePickerInlineKeyboardMarkup(ReviewRequest reviewRequest) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        LocalDate currentDay = LocalDate.now(ZoneId.of("Europe/Moscow"));
        addDateButton(keyboardRows, reviewRequest, currentDay);
        addDateButton(keyboardRows, reviewRequest, currentDay.plusDays(1));
        addDateButton(keyboardRows, reviewRequest, currentDay.plusDays(2));
        addCancelButton(keyboardRows, reviewRequest);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }

    private void addDateButton(List<List<InlineKeyboardButton>> keyboardRows, ReviewRequest reviewRequest, LocalDate date) {
        String dateString = date.format(defaultDateFormatter());
        InlineKeyboardButton dateButton = new InlineKeyboardButton();
        dateButton.setText(dateString);
        dateButton.setCallbackData(String.join(" ", CallBack.SET_REVIEW_REQUEST_DATE_TIME.getAlias(), dateString, reviewRequest.getId().toString()));
        keyboardRows.add(Collections.singletonList(dateButton));
    }

    private void addCancelButton(List<List<InlineKeyboardButton>> keyboardRows, ReviewRequest reviewRequest) {
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отмена");
        cancelButton.setCallbackData(String.join(" ", CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias(), reviewRequest.getId().toString()));
        keyboardRows.add(Collections.singletonList(cancelButton));
    }
}