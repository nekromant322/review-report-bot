package com.nekromant.telegram.commands.review;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.utils.ValidationUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REVIEW;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_HELP_MESSAGE;
import static com.nekromant.telegram.contants.MessageContants.REVIEW_REQUEST_SENT;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class ReviewCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    public ReviewCommand() {
        super(REVIEW.getAlias(), REVIEW.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String studentChatId = chat.getId().toString();
        message.setChatId(studentChatId);

        ReviewRequest reviewRequest = new ReviewRequest();
        try {
            ValidationUtils.validateArguments(arguments);
            reviewRequest.setStudentUserName(user.getUserName());
            reviewRequest.setStudentChatId(studentChatId);
            reviewRequest.setDate(parseDate(arguments));
            reviewRequest.setTitle(parseTitle(arguments));
            reviewRequest.setTimeSlots(parseTimeSlots(arguments));

        } catch (Exception e) {
            e.printStackTrace();
            message.setText(ERROR + REVIEW_HELP_MESSAGE);
            execute(absSender, message, user);
            return;
        }
        System.out.println("Сохранение нового реквеста " + reviewRequest.toString());
        reviewRequestRepository.save(reviewRequest);

        writeMentors(absSender, user, specialChatService.getMentorsChatId(), reviewRequest);


        message.setText(REVIEW_REQUEST_SENT);
        execute(absSender, message, user);
    }

    private LocalDate parseDate(String[] strings) {

        if (strings[0].equalsIgnoreCase("сегодня")) {
            return LocalDate.now(ZoneId.of("Europe/Moscow"));
        }
        if (strings[0].equalsIgnoreCase("завтра")) {
            return LocalDate.now(ZoneId.of("Europe/Moscow")).plus(1, DAYS);
        }
        throw new InvalidParameterException();
    }

    private Set<Integer> parseTimeSlots(String[] strings) {
        Set<Integer> timeSlots = new HashSet<>();
        for (int i = 1; i < strings.length; i++) {
            if (!strings[i].toLowerCase().contains("тема")) {
                timeSlots.add(Integer.parseInt(strings[i]));
                if (Integer.parseInt(strings[i]) > 24 || Integer.parseInt(strings[i]) < 0) {
                    throw new InvalidParameterException();
                }
            } else {
                return timeSlots;
            }
        }
        return timeSlots;
    }

    private String parseTitle(String[] strings) {
        for (int i = 1; i < strings.length; i++) {
            if (strings[i].toLowerCase().contains("тема")) {
                return Arrays.stream(strings).skip(i).collect(Collectors.joining(" "));
            }
        }
        return "";
    }

    @SneakyThrows
    private void writeMentors(AbsSender absSender, User user, String mentorsChatId, ReviewRequest reviewRequest) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();


        reviewRequest.getTimeSlots().forEach(x -> {
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(x + ":00");
            inlineKeyboardButton.setCallbackData(CallBack.APPROVE.getAlias() + " " + reviewRequest.getId() + " " + x);

            keyboardButtonRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonRow);
        });

        List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Отменить");
        inlineKeyboardButton.setCallbackData(CallBack.DENY.getAlias() + " " + reviewRequest.getId());

        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);

        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage message = new SendMessage();
        message.setChatId(mentorsChatId);


        message.setText("@" + reviewRequest.getStudentUserName() + "\n" + reviewRequest.getTitle() + "\n" +
                reviewRequest.getDate().format(defaultDateFormatter()) + "\n");
        message.setReplyMarkup(inlineKeyboardMarkup);

        Message executedMessage = absSender.execute(message);
        reviewRequest.setPollMessageId(executedMessage.getMessageId());
        reviewRequestRepository.save(reviewRequest);
    }

}
