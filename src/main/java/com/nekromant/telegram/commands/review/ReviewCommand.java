package com.nekromant.telegram.commands.review;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.utils.SendMessageFactory;
import com.nekromant.telegram.utils.ValidationUtils;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REVIEW;
import static com.nekromant.telegram.contants.MessageContants.*;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Slf4j
@Component
public class ReviewCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    @Autowired
    public ReviewCommand() {
        super(REVIEW.getAlias(), REVIEW.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (chat.isGroupChat() || chat.isSuperGroupChat()) {
            sendAnswer(chat.getId().toString(), GROUP_CHAT_IS_NOT_SUPPORTED, absSender, user);
        } else {
            SendMessage message = new SendMessage();
            String studentChatId = chat.getId().toString();
            message.setChatId(studentChatId);

            ReviewRequest reviewRequest = new ReviewRequest();
            try {
                ValidationUtils.validateArgumentsNumber(arguments);
                reviewRequest.setStudentUserName(user.getUserName());
                reviewRequest.setStudentChatId(studentChatId);
                reviewRequest.setTitle(parseTitle(arguments));
                reviewRequest.setTimeSlots(parseTimeSlots(arguments));

                log.info("Сохранение нового реквеста {}", reviewRequest);
                reviewRequestRepository.save(reviewRequest);
                sendDatePicker(absSender, user.getId().toString(), reviewRequest);
            } catch (NumberFormatException e) {
                log.error("Таймслот должен быть указан целым числом. {}", e.getMessage());
                message.setText("Таймслот должен быть указан целым числом\n" + REVIEW_HELP_MESSAGE);
                execute(absSender, message, user);
            } catch (InvalidParameterException e) {
                log.error("Неверный аргумент был передан в команду. {}", e.getMessage());
                message.setText(e.getMessage() + "\n" + REVIEW_HELP_MESSAGE);
                execute(absSender, message, user);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                message.setText(ERROR + REVIEW_HELP_MESSAGE);
                execute(absSender, message, user);
            }
        }
    }

    private void sendAnswer(String chatId, String text, AbsSender absSender, User user) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(absSender, message, user);
    }

    private Set<Integer> parseTimeSlots(String[] strings) {
        Set<Integer> timeSlots = new HashSet<>();
        for (String string : strings) {
            if (!string.toLowerCase().contains("тема")) {
                timeSlots.add(Integer.parseInt(string));
                if (Integer.parseInt(string) > 24 || Integer.parseInt(string) < 0) {
                    throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 23");
                }
            } else {
                return timeSlots;
            }
        }
        return timeSlots;
    }

    private String parseTitle(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].toLowerCase().contains("тема")) {
                return Arrays.stream(strings).skip(i).collect(Collectors.joining(" "));
            }
        }
        return "";
    }

    private void sendDatePicker(AbsSender absSender, String userChatId, ReviewRequest reviewRequest) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = getDatePickerInlineKeyboardMarkup(reviewRequest);
        SendMessage message = sendMessageFactory.create(userChatId, "Выберите дату");
        message.setReplyMarkup(inlineKeyboardMarkup);
        absSender.execute(message);
    }

    private InlineKeyboardMarkup getDatePickerInlineKeyboardMarkup(ReviewRequest reviewRequest) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        LocalDate currentDay = LocalDate.now(ZoneId.of("Europe/Moscow"));
        addDateButton(keyboardRows, reviewRequest, currentDay);
        addDateButton(keyboardRows, reviewRequest, currentDay.plusDays(1));
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
