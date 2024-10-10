package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.Report;
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
public class ReportDateTimePicker {
    @Autowired
    private SendMessageFactory sendMessageFactory;

    public SendMessage getDatePickerSendMessage(String userChatId, Report report, Integer messageId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getDatePickerInlineKeyboardMarkup(report, messageId);
        SendMessage message = sendMessageFactory.create(userChatId, "Выберите дату");
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    private InlineKeyboardMarkup getDatePickerInlineKeyboardMarkup(Report report, Integer messageId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        LocalDate currentDay = LocalDate.now(ZoneId.of("Europe/Moscow"));
        addDateButton(keyboardRows, report, currentDay.minusDays(1), messageId);
        addDateButton(keyboardRows, report, currentDay, messageId);
        addCancelButton(keyboardRows, report);

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        return inlineKeyboardMarkup;
    }

    private void addDateButton(List<List<InlineKeyboardButton>> keyboardRows, Report report, LocalDate date, Integer messageId) {
        String dateString = date.format(defaultDateFormatter());
        InlineKeyboardButton dateButton = new InlineKeyboardButton();
        dateButton.setText(dateString);
        dateButton.setCallbackData(String.join(" ", CallBack.SET_REPORT_DATE_TIME.getAlias(), dateString, report.getId().toString(), messageId.toString()));
        keyboardRows.add(Collections.singletonList(dateButton));
    }

    private void addCancelButton(List<List<InlineKeyboardButton>> keyboardRows, Report report) {
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отмена");
        cancelButton.setCallbackData(String.join(" ", CallBack.DENY_REPORT_DATE_TIME.getAlias(), report.getId().toString()));
        keyboardRows.add(Collections.singletonList(cancelButton));
    }
}
