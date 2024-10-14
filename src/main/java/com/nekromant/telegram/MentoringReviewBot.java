package com.nekromant.telegram;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.commands.MentoringReviewWithMessageIdCommand;
import com.nekromant.telegram.commands.report.ReportDateTimePicker;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import com.nekromant.telegram.service.ReportService;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.update_handler.NonCommandUpdateHandler;
import com.nekromant.telegram.service.update_handler.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Component
@Slf4j
public class MentoringReviewBot extends TelegramLongPollingCommandBot {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    private final NonCommandUpdateHandler nonCommandUpdateHandler;

    @Autowired
    public MentoringReviewBot(List<MentoringReviewCommand> allCommands,
                              List<MentoringReviewWithMessageIdCommand> allWithMessageIdCommands,
                              List<CallbackStrategy> callbackStrategies,
                              SpecialChatService specialChatService,
                              SendMessageFactory sendMessageFactory,
                              ChatMessageRepository chatMessageRepository,
                              ReviewRequestRepository reviewRequestRepository,
                              UserInfoRepository userInfoRepository,
                              ReportService reportService,
                              ReportRepository reportRepository,
                              ReportDateTimePicker reportDateTimePicker) {
        super();
        allCommands.forEach(this::register);
        allWithMessageIdCommands.forEach(this::register);
        this.nonCommandUpdateHandler = new NonCommandUpdateHandler(
                new SendMessageService(this, sendMessageFactory),
                callbackStrategies,
                specialChatService,
                sendMessageFactory,
                chatMessageRepository,
                reviewRequestRepository,
                userInfoRepository,
                reportService,
                reportRepository,
                reportDateTimePicker);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        nonCommandUpdateHandler.handleUpdate(update);
    }
}