package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.MentorsChat;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.MentorsChatRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.MentorsChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReviewCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private MentorsChatService mentorsChatService;

    @Autowired
    public ReviewCommand() {
        super("review", "Забукать ревью");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String studentChatId = chat.getId().toString();
        message.setChatId(studentChatId);

        ReviewRequest reviewRequest = new ReviewRequest();
        try {
            validateArguments(arguments);
            reviewRequest.setStudentUserName(user.getUserName());
            reviewRequest.setStudentChatId(studentChatId);
            reviewRequest.setDate(parseDate(arguments));
            reviewRequest.setTitle(parseTitle(arguments));
            reviewRequest.setTimeSlots(parseTimeSlots(arguments));

        } catch (Exception e) {
            message.setText("Для того чтобы попросить ревью напишите что-то вроде /review 26.05.2021 15 17 18 Тема: 5 модуль" +
                    "\n15 17 18 - таймслоты" +
                    "\nслово \"Тема\" обязательно, потому что разработчик лентяй");
            execute(absSender, message, user);
            return;
        }
        System.out.println("Сохранение нового реквеста " + reviewRequest.toString());
        reviewRequestRepository.save(reviewRequest);

        writeMentors(absSender, user, chat, mentorsChatService.getMentorsChatId(), reviewRequest);


        message.setText("Запрос отправлен менторам, ответ скоро придет");
        execute(absSender, message, user);
    }

    private LocalDate parseDate(String[] strings) {

        return LocalDate.parse(strings[0], DateTimeFormatter.ofPattern("dd.MM.yyyy"));
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

    private void validateArguments(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException();
        }
    }

    private void writeMentors(AbsSender absSender, User user, Chat chat, String mentorsChatId, ReviewRequest reviewRequest) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();


        reviewRequest.getTimeSlots().forEach( x -> {
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(x + ":00");
            inlineKeyboardButton.setCallbackData("/approve " + reviewRequest.getId() + " " + x);

            keyboardButtonRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonRow);
        });

        List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Отменить");
        inlineKeyboardButton.setCallbackData("/deny " + reviewRequest.getId());

        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);






        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage message = new SendMessage();
        message.setChatId(mentorsChatId);

//        final StringBuilder commands = new StringBuilder();
//        reviewRequest.getTimeSlots().forEach(x ->   commands.append("/approve " + reviewRequest.getId() + " " + x + "\n"));
//
//        commands.append("/deny " + reviewRequest.getId());

        message.setText("@" + reviewRequest.getStudentUserName() + "\n" + reviewRequest.getTitle() + "\n" + reviewRequest.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                "\n");
        message.setReplyMarkup(inlineKeyboardMarkup);


        execute(absSender, message, user);
    }

}
