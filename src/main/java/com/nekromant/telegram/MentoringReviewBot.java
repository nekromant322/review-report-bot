package com.nekromant.telegram;

import com.nekromant.telegram.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.commands.MentoringReviewWithMessageIdCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.repository.ChatMessageRepository;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
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
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final Map<CallBack, CallbackStrategy> callbackStrategyMap;

    @Autowired
    public MentoringReviewBot(List<MentoringReviewCommand> allCommands, List<MentoringReviewWithMessageIdCommand> allWithMessageIdCommands, List<CallbackStrategy> callbackStrategies) {
        super();
        this.callbackStrategyMap = callbackStrategies.stream()
                .collect(Collectors.toMap(CallbackStrategy::getPrefix, Function.identity()));
        allCommands.forEach(this::register);
        allWithMessageIdCommands.forEach(this::register);
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
        Map<ChatType, SendMessage> messageByChatTypeMap = getMessageByChatTypeMap(update);

        CallbackStrategy strategy = getCallbackStrategy(update);
        DeleteMessageStrategy deleteMessageStrategy = new DeleteMessageStrategy();
        strategy.executeCallbackQuery(update, messageByChatTypeMap, deleteMessageStrategy);

        deleteReplyMessage(update, deleteMessageStrategy.getMessagePart());

        sendMessagesIfNotEmpty(messageByChatTypeMap, update);
    }

    private Map<ChatType, SendMessage> getMessageByChatTypeMap(Update update) {
        Map<ChatType, SendMessage> sendMessageMap = new HashMap<>();
        sendMessageMap.put(ChatType.USER_CHAT, sendMessageFactory.createFromUpdate(update, ChatType.USER_CHAT));
        sendMessageMap.put(ChatType.MENTORS_CHAT, sendMessageFactory.createFromUpdate(update, ChatType.MENTORS_CHAT));
        sendMessageMap.put(ChatType.REPORTS_CHAT, sendMessageFactory.createFromUpdate(update, ChatType.REPORTS_CHAT));
        return sendMessageMap;
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

    private void deleteReplyMessage(Update update, MessagePart messagePart) {
        switch (messagePart) {
            case MARKUP:
                deleteMessageMarkUp(update);
                break;
            case ENTIRE_MESSAGE:
                deleteCallbackMessage(update);
                break;
            default:
                log.error("Unsupported delete message strategy: {}", messagePart.name());
                throw new RuntimeException("Unsupported delete message strategy: " + messagePart.name());
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

    private void sendMessagesIfNotEmpty(Map<ChatType, SendMessage> messages, Update update) {
        messages.forEach((chatType, message) -> {
            if (isNotEmptyMessage(message)) {
                try {
                    String callbackData = update.getCallbackQuery().getData();
                    if (isReportUpdate(callbackData)) {
                        updateReportMessage(chatType, message, callbackData);
                    } else {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
                }
            }
        });
    }

    private void updateReportMessage(ChatType chatType, SendMessage message, String callbackData) throws TelegramApiException {
        Integer messageId = extractMessageIdFromCallbackData(callbackData);
        ChatMessage chatMessage = chatMessageRepository.findByUserMessageId(messageId);

        if (chatMessage != null) {
            if (isReportUpdated(message)) {
                updateReportText(chatType, message, chatMessage);
            } else {
                sendNewReportSavedAndSetBotMessageId(chatType, message, chatMessage);
            }
        } else {
            execute(message);
        }
    }

    private void sendNewReportSavedAndSetBotMessageId(ChatType chatType, SendMessage message, ChatMessage chatMessage) throws TelegramApiException {
        Message executedMessage = execute(message);

        if (chatType == ChatType.REPORTS_CHAT) {
            chatMessage.setReportChatBotMessageId(executedMessage.getMessageId());
        } else if (chatType == ChatType.USER_CHAT) {
            chatMessage.setUserChatBotMessageId(executedMessage.getMessageId());
        }

        chatMessageRepository.save(chatMessage);
    }

    private void updateReportText(ChatType chatType, SendMessage message, ChatMessage chatMessage) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(message.getText());
        editMessageText.setChatId(message.getChatId());

        if (chatType == ChatType.REPORTS_CHAT) {
            editMessageText.setMessageId(chatMessage.getReportChatBotMessageId());
        } else {
            editMessageText.setMessageId(chatMessage.getUserChatBotMessageId());
        }

        execute(editMessageText);
    }

    private static boolean isReportUpdated(SendMessage message) {
        return message.getText().contains("Отчёт обновлен");
    }

    private static boolean isReportUpdate(String callbackData) {
        return callbackData.split(" ")[0].equalsIgnoreCase(CallBack.SET_REPORT_DATE_TIME.getAlias()) || !callbackData.split(" ")[0].equalsIgnoreCase(CallBack.DENY_REPORT.getAlias());
    }

    private Integer extractMessageIdFromCallbackData(String callbackData) {
        return Integer.parseInt(callbackData.split(" ")[3]);
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
