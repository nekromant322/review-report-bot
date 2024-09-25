package com.nekromant.telegram;

import com.nekromant.telegram.callback_strategy.*;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private DeleteMessageStrategyComponent deleteMessageStrategyComponent;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    private final Map<CallBack, CallbackStrategy> callbackStrategyMap;

    @Autowired
    public MentoringReviewBot(List<MentoringReviewCommand> allCommands, List<CallbackStrategy> callbackStrategies) {
        super();
        this.callbackStrategyMap = callbackStrategies.stream()
                .collect(Collectors.toMap(CallbackStrategy::getPrefix, Function.identity()));
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

    private void processUserMessage(Update update) {
        if (!isSpecialChat(update)) {
            sendMessage(update);
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        SendMessage userMessage = sendMessageFactory.createFromUpdate(update, ChatType.USER_CHAT);
        SendMessage mentorsMessage = sendMessageFactory.createFromUpdate(update, ChatType.MENTORS_CHAT);
        SendMessage reportsChatMessage = sendMessageFactory.createFromUpdate(update, ChatType.REPORTS_CHAT);

        CallbackStrategy strategy = getCallbackStrategy(update);
        strategy.executeCallbackQuery(update, userMessage, mentorsMessage, reportsChatMessage, deleteMessageStrategyComponent);

        deleteReplyMessage(update);

        sendMessagesIfNotEmpty(userMessage, mentorsMessage, reportsChatMessage);
    }

    public void processEditedMessageUpdate(Update update) {
        update.setMessage(update.getEditedMessage());
        super.onUpdateReceived(update);
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

    private static boolean isNotEmptyMessage(SendMessage sendMessage) {
        return sendMessage.getText() != null && !sendMessage.getText().isEmpty();
    }

    private CallbackStrategy getCallbackStrategy(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String command = callbackData.split(" ")[0];

        return callbackStrategyMap.get(CallBack.from(command));
    }

    private boolean isCallbackQuery(Update update) {
        return update.hasCallbackQuery();
    }

    private boolean isEditedMessage(Update update) {
        return update.hasEditedMessage();
    }

    private void deleteReplyMessage(Update update) {
        DeleteMessageStrategy strategy = deleteMessageStrategyComponent.getDeleteMessageStrategy();
        switch (strategy) {
            case MARKUP:
                deleteMessageMarkUp(update);
                break;
            case ENTIRE_MESSAGE:
                deleteCallbackMessage(update);
                break;
            default:
                throw new RuntimeException("Unsupported delete message strategy: " + strategy.name());
        }
    }

    private void deleteCallbackMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        Message message = update.getCallbackQuery().getMessage();
        deleteMessage.setChatId(message.getChatId().toString());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to delete callback message: {}", e.getMessage(), e);
        }
    }

    private void deleteMessageMarkUp(Update update) {
        EditMessageReplyMarkup message = new EditMessageReplyMarkup();
        Message callbackMessage = update.getCallbackQuery().getMessage();
        message.setChatId(callbackMessage.getChatId().toString());
        message.setMessageId(callbackMessage.getMessageId());
        message.setReplyMarkup(null);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to delete message markup: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessagesIfNotEmpty(SendMessage... messages) {
        for (SendMessage message : messages) {
            if (isNotEmptyMessage(message)) {
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
                }
            }
        }
    }

    private void sendMessage(Update update) {
        SendMessage sendMessage = sendMessageFactory.create(String.valueOf(update.getMessage().getChatId()), UNKNOWN_COMMAND);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
        }
    }

    @SneakyThrows
    public void sendMessage(String chatId, String text) {
        SendMessage message = sendMessageFactory.create(chatId, text);
        message.disableWebPagePreview();
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения {}", e.getMessage());
        }

    }
}
