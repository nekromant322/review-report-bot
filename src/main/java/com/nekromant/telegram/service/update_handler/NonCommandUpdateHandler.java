package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.commands.report.ReportDateTimePicker;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import com.nekromant.telegram.service.ReportService;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.update_handler.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Service
public class NonCommandUpdateHandler {

    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final EditedMessageHandler editedMessageHandler;
    private final SpecialChatService specialChatService;

    @Autowired
    public NonCommandUpdateHandler(SendMessageService sendMessageService,
                                   List<CallbackStrategy> callbackStrategies,
                                   SpecialChatService specialChatService,
                                   SendMessageFactory sendMessageFactory,
                                   ChatMessageRepository chatMessageRepository,
                                   ReviewRequestRepository reviewRequestRepository,
                                   UserInfoRepository userInfoRepository,
                                   ReportService reportService,
                                   ReportRepository reportRepository,
                                   ReportDateTimePicker reportDateTimePicker) {
        this.messageHandler = new MessageHandler(sendMessageService,
                sendMessageFactory);
        this.callbackQueryHandler = new CallbackQueryHandler(callbackStrategies,
                sendMessageService,
                sendMessageFactory,
                chatMessageRepository,
                specialChatService,
                reviewRequestRepository,
                userInfoRepository);
        this.editedMessageHandler = new EditedMessageHandler(sendMessageService,
                chatMessageRepository,
                reportService,
                reportRepository,
                reportDateTimePicker,
                sendMessageFactory,
                specialChatService);
        this.specialChatService = specialChatService;
    }


    public void handleUpdate(Update update) {
        if (isCallbackQuery(update)) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            callbackQueryHandler.handleCallbackQuery(callbackQuery);
        }
        if (isUserMessage(update)) {
            Message message = update.getMessage();
            messageHandler.handleMessage(message);
        }
        if (isEditedMessage(update)) {
            Message editedMessage = update.getEditedMessage();
            editedMessageHandler.handleEditedMessage(editedMessage);
        }
    }

    private boolean isUserMessage(Update update) {
        return update.hasMessage() && update.getMessage().isUserMessage() && !isSpecialChat(update);
    }

    private boolean isSpecialChat(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        Chat chat = update.getMessage().getChat();

        return isMentorsChat(chatId) || isReportsChat(chatId) || isJavaCommunityChat(chat);
    }

    private boolean isMentorsChat(String chatId) {
        return chatId.equals(specialChatService.getMentorsChatId());
    }

    private boolean isReportsChat(String chatId) {
        return chatId.equals(specialChatService.getReportsChatId());
    }

    private boolean isJavaCommunityChat(Chat chat) {
        return (chat.isGroupChat() || chat.isSuperGroupChat()) && chat.getTitle().equals("java-кумунити");
    }

    private boolean isCallbackQuery(Update update) {
        return update.hasCallbackQuery();
    }

    private boolean isEditedMessage(Update update) {
        return update.hasEditedMessage();
    }
}
