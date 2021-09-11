package com.nekromant.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nekromant.telegram.commands.ApproveCommand;
import com.nekromant.telegram.commands.DenyCommand;
import com.nekromant.telegram.commands.GetMentorsCommand;
import com.nekromant.telegram.commands.RegisterMentorsChatCommand;
import com.nekromant.telegram.commands.ReviewCommand;
import com.nekromant.telegram.commands.AddMentorsCommand;
import com.nekromant.telegram.commands.StartCommand;
import com.nekromant.telegram.model.MentorsChat;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorsChatRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.MentorsChatService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.security.InvalidParameterException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class MentoringReviewBot extends TelegramLongPollingCommandBot {

    @Autowired
    private MentorsChatService mentorsChatService;

//    Map<String, String> messages = new HashMap<>();

    private ReplyMessageService replyMessageService;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private ApproveCommand approveCommand;

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;


    @Autowired
    public MentoringReviewBot(StartCommand startCommand,
                              ReplyMessageService replyMessageService,
                              ReviewCommand reviewCommand,
                              AddMentorsCommand addMentorsCommand,
                              GetMentorsCommand getMentorsCommand,
                              RegisterMentorsChatCommand registerMentorsChatCommand,
                              ApproveCommand approveCommand,
                              DenyCommand denyCommand) {
        super();
        this.replyMessageService = replyMessageService;
        registerAll(startCommand, reviewCommand, addMentorsCommand, getMentorsCommand, registerMentorsChatCommand, approveCommand,
                denyCommand);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @SneakyThrows
    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            if(!update.getMessage().getChatId().equals(mentorsChatService.getMentorsChatId())) {
                sendMessage(update);
            }
        } else if (update.hasCallbackQuery()) {

            String callBackData = update.getCallbackQuery().getData();
            SendMessage message = new SendMessage();
            SendMessage messageForMentors = new SendMessage();
            messageForMentors.setChatId(mentorsChatService.getMentorsChatId());

            if(callBackData.startsWith("/approve")) {



                Long reviewId = Long.parseLong(callBackData.split(" ")[1]);
                Integer timeSlot = Integer.parseInt(callBackData.split(" ")[2]);
                ReviewRequest review = reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
                review.setBookedTimeSlot(timeSlot);
                review.setMentorUserName(update.getCallbackQuery().getFrom().getUserName());
                reviewRequestRepository.save(review);
                message.setChatId(review.getStudentChatId());
                message.setText("Ревью c " + "@"+review.getMentorUserName() + " назначено на \n"+ review.getDate().format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " " + review.getBookedTimeSlot()+":00" + "\n" + review.getTitle());

                message.enableMarkdown(false);

                messageForMentors.setText("@" + update.getCallbackQuery().getFrom().getUserName() + " апрувнул ревью с @" + review.getStudentUserName() + " в " + timeSlot + ":00");
                messageForMentors.enableMarkdown(false);
            }
            if(callBackData.startsWith("/deny")) {


                Long reviewId = Long.parseLong(callBackData.split(" ")[1]);

                ReviewRequest review = reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
                message.setChatId(review.getStudentChatId());
                message.setText("Никто не может провести ревью "+ review.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " "
                        + review.getTimeSlots().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(":00, "))+":00" + "\n");

                reviewRequestRepository.deleteById(reviewId);
                message.enableMarkdown(false);

                messageForMentors.setText("@" + update.getCallbackQuery().getFrom().getUserName() + " отменил ревью с @" + review.getStudentUserName());
                messageForMentors.enableMarkdown(false);
            }
            try {
                execute(message);
                execute(messageForMentors);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessage(Update update) throws JsonProcessingException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Не понимаю команду");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
