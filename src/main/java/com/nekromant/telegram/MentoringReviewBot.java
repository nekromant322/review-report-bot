package com.nekromant.telegram;

import com.nekromant.telegram.callback_strategy.*;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.service.SpecialChatService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.nekromant.telegram.contants.MessageContants.UNKNOWN_COMMAND;


@Component
@Slf4j
public class MentoringReviewBot extends TelegramLongPollingCommandBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private SpecialChatService specialChatService;
    @Autowired
    private ApproveCallbackStrategy approveCallbackStrategy;
    @Autowired
    private DenyCallbackStrategy denyCallbackStrategy;
    @Autowired
    private DateTimeCallbackStrategy dateTimeCallbackStrategy;
    @Autowired
    private DenyReportCallbackStrategy denyReportCallbackStrategy;
    @Autowired
    private DeleteMessageStrategyComponent deleteMessageStrategyComponent;

    @Autowired
    public MentoringReviewBot(List<MentoringReviewCommand> allCommands) {
        super();
        allCommands.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @SneakyThrows
    @Override
    public void processNonCommandUpdate(Update update) {
        if (isUserMessage(update)) {
            processUserMessage(update);
        } else if (isCallbackQuery(update)) {
            handleCallbackQuery(update);
        } else if (isEditedMessage(update)) {
            processEditedMessageUpdate(update);
        }
    }

    private boolean isUserMessage(Update update) {
        return update.hasMessage() && update.getMessage().isUserMessage() && !isSpecialChat(update);
    }

    private boolean isSpecialChat(Update update) {
        return update.getMessage().getChatId().toString().equals(specialChatService.getMentorsChatId()) ||
                update.getMessage().getChatId().toString().equals(specialChatService.getReportsChatId()) ||
                ((update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat()) && update.getMessage().getChat().getTitle().equals("java-кумунити"));
    }

    private void processUserMessage(Update update) {
        if (!isSpecialChat(update)) {
            sendMessage(update);
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        SendMessage messageForUser = new SendMessage();
        SendMessage messageForMentors = new SendMessage();
        SendMessage messageForReportsChat = new SendMessage();
        setChatIdForUser(update, messageForUser);
        setChatIdForMentors(messageForMentors);
        setChatIdForReportsChat(messageForReportsChat);


        CallbackStrategy strategy = getCallbackStrategy(callbackData);
        strategy.executeCallbackQuery(update, callbackData, messageForUser, messageForMentors, messageForReportsChat, deleteMessageStrategyComponent);

        deleteReplyMessage(update);

        try {
            if (isNotEmptyMessage(messageForUser)) {
                execute(messageForUser);
            }
            if (isNotEmptyMessage(messageForMentors)) {
                execute(messageForMentors);
            }
            if (isNotEmptyMessage(messageForReportsChat)) {
                execute(messageForReportsChat);
            }
        } catch (TelegramApiException e) {
            log.error("Failed to execute callback query", e);
        }
    }

    private void deleteReplyMessage(Update update) {
        DeleteMessageStrategy chosenDeleteMessageStrategy = deleteMessageStrategyComponent.getDeleteMessageStrategy();
        if (chosenDeleteMessageStrategy == DeleteMessageStrategy.MARKUP) {
            deleteMessageMarkUp(update);
        } else if (chosenDeleteMessageStrategy == DeleteMessageStrategy.ENTIRE_MESSAGE) {
            deleteCallbackMessage(update);
        } else {
            throw new RuntimeException("Invalid delete message strategy: " + chosenDeleteMessageStrategy.name());
        }
    }

    private static boolean isNotEmptyMessage(SendMessage sendMessage) {
        return sendMessage.getText() != null && !sendMessage.getText().isEmpty();
    }

    private CallbackStrategy getCallbackStrategy(String callbackData) {
        String callbackCommandName = callbackData.split(" ")[0];
        if (CallBack.APPROVE.equals(CallBack.from(callbackCommandName))) {
            return approveCallbackStrategy;
        } else if (CallBack.DENY.equals(CallBack.from(callbackCommandName))) {
            return denyCallbackStrategy;
        } else if (CallBack.DATE_TIME.equals(CallBack.from(callbackCommandName))) {
            return dateTimeCallbackStrategy;
        } else if (CallBack.DENY_REPORT.equals(CallBack.from(callbackCommandName))) {
            return denyReportCallbackStrategy;
        }
        throw new IllegalArgumentException("Invalid callback data: " + callbackData);
    }

    private void setChatIdForUser(Update update, SendMessage messageForUser) {
        messageForUser.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
    }

    private void setChatIdForMentors(SendMessage messageForMentors) {
        messageForMentors.setChatId(specialChatService.getMentorsChatId());
    }

    private void setChatIdForReportsChat(SendMessage messageForReportsChat) {
        messageForReportsChat.setChatId(specialChatService.getReportsChatId());
    }

    private boolean isCallbackQuery(Update update) {
        return update.hasCallbackQuery();
    }

    private boolean isEditedMessage(Update update) {
        return update.hasEditedMessage();
    }

    public void processEditedMessageUpdate(Update update) {
        update.setMessage(update.getEditedMessage());
        super.onUpdateReceived(update);
    }

    @SneakyThrows
    private void deleteCallbackMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        Message message = update.getCallbackQuery().getMessage();
        deleteMessage.setChatId(message.getChatId().toString());
        deleteMessage.setMessageId(message.getMessageId());
        execute(deleteMessage);
    }

    @SneakyThrows
    private void deleteMessageMarkUp(Update update) {
        EditMessageReplyMarkup message = new EditMessageReplyMarkup();
        Message callbackMessage = update.getCallbackQuery().getMessage();
        message.setChatId(callbackMessage.getChatId().toString());
        message.setMessageId(callbackMessage.getMessageId());
        message.setReplyMarkup(null);
        execute(message);

    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessage(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText(UNKNOWN_COMMAND);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public void sendMessage(String chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            //убирает превьюшки ссылок
            message.disableWebPagePreview();
            message.setText(text);
            message.setChatId(chatId);
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения {}", e.getMessage());
        }

    }
}
