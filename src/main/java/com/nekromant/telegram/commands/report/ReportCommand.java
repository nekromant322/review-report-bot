package com.nekromant.telegram.commands.report;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REPORT;
import static com.nekromant.telegram.contants.MessageContants.*;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static com.nekromant.telegram.utils.ValidationUtils.validateArgumentsNumber;

@Slf4j
@Component
public class ReportCommand extends MentoringReviewCommand {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserInfoService userInfoService;

    public ReportCommand() {
        super(REPORT.getAlias(), REPORT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            sendAnswer(chat.getId().toString(), GROUP_CHAT_IS_NOT_SUPPORTED, absSender, user);
        } else {
            try {

                validateArgumentsNumber(strings);

                //тут убрать когда получу все chatId, оставить только в /start
                userInfoService.initializeUserInfo(chat, user);

                Report report = new Report();
                report.setHours(parseHours(strings));
                report.setStudentUserName(user.getUserName());
                report.setTitle(parseTitle(strings));

                if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
                    log.info(TOO_MANY_REPORTS + "\n{}", report);
                }
                reportRepository.save(report);

                sendDatePicker(absSender, user.getId().toString(), report);

            } catch (InvalidParameterException e) {
                log.error(e.getMessage(), e);
                sendAnswer(chat.getId().toString(), e.getMessage() + "\n" + REPORT_HELP_MESSAGE, absSender, user);
            } catch (NumberFormatException e) {
                log.error("Часы должны быть указаны целым числом. {}", e.getMessage());
                sendAnswer(chat.getId().toString(), "Часы должны быть указаны целым числом\n" + REPORT_HELP_MESSAGE, absSender, user);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendAnswer(chat.getId().toString(), ERROR + REPORT_HELP_MESSAGE, absSender, user);
            }
        }
    }

    private void sendAnswer(String chatId, String text, AbsSender absSender, User user) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(absSender, message, user);
    }

    private String parseTitle(String[] strings) {
        return Arrays.stream(strings).skip(1).collect(Collectors.joining(" "));
    }

    private Integer parseHours(String[] strings) {
        int hours = Integer.parseInt(strings[0]);
        validateHoursArgument(hours);
        return hours;
    }

    private void validateHoursArgument(int hours) {
        if (hours < 0 || hours > 24) {
            throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 24");
        }
    }

    private void sendDatePicker(AbsSender absSender, String userChatId, Report report) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = getDatePickerInlineKeyboardMarkup(report);
        SendMessage message = new SendMessage();
        message.setChatId(userChatId);
        message.setText("Выберите дату");
        message.setReplyMarkup(inlineKeyboardMarkup);

        absSender.execute(message);
    }

    private static InlineKeyboardMarkup getDatePickerInlineKeyboardMarkup(Report report) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        String today = LocalDate.now(ZoneId.of("Europe/Moscow")).format(defaultDateFormatter());
        inlineKeyboardButton.setText(today);
        inlineKeyboardButton.setCallbackData(CallBack.DATE_TIME.getAlias() + " " + today + " " + report.getId());
        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);

        keyboardButtonRow = new ArrayList<>();
        inlineKeyboardButton = new InlineKeyboardButton();
        String yesterday = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1).format(defaultDateFormatter());
        inlineKeyboardButton.setText(yesterday);
        inlineKeyboardButton.setCallbackData(CallBack.DATE_TIME.getAlias() + " " + yesterday + " " + report.getId());
        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);

        keyboardButtonRow = new ArrayList<>();
        inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Отмена");
        inlineKeyboardButton.setCallbackData(CallBack.DENY_REPORT.getAlias() + " " + report.getId());
        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
