package com.nekromant.telegram;

import com.nekromant.telegram.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.commands.MentoringReviewWithMessageIdCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.MessageContants.UNKNOWN_COMMAND;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;


@Component
@Slf4j
public class MentoringReviewBot extends TelegramLongPollingCommandBot {

    private static final int MIDNIGHT = 24;
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
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

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
        String callbackData = update.getCallbackQuery().getData();

        CallbackStrategy strategy = getCallbackStrategy(callbackData);
        DeleteMessageStrategy deleteMessageStrategy = new DeleteMessageStrategy();
        strategy.executeCallbackQuery(update, messageByChatTypeMap, deleteMessageStrategy);

        deleteReplyMessage(update, deleteMessageStrategy.getMessagePart());

        sendMessagesIfNotEmpty(messageByChatTypeMap, callbackData);
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

    private CallbackStrategy getCallbackStrategy(String callbackData) {
        return callbackStrategyMap.get(CallBack.from(callbackData.split(" ")[0]));
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

    private void sendMessagesIfNotEmpty(Map<ChatType, SendMessage> messages, String callbackData) {
        messages.forEach((chatType, message) -> {
            if (isNotEmptyMessage(message)) {
                try {
                    String callbackAlias = callbackData.split(" ")[0];
                    if (isNewOrEditedReport(callbackAlias)) {
                        updateReportMessage(chatType, message, callbackData);
                    } else if (isReviewRequest(callbackAlias)) {
                        execute(message);
                        if (isNotDenyForReviewRequest(callbackAlias)) {
                            writeMentors(callbackData); // [2]
                        }
                    } else {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
                }
            }
        });
    }

    private static boolean isNotDenyForReviewRequest(String callbackAlias) {
        return !callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
    }

    private boolean isReviewRequest(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REVIEW_REQUEST_DATE_TIME.getAlias())
                || callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
    }

    @SneakyThrows
    private void writeMentors(String callbackData) {
        String mentorsChatId = specialChatService.getMentorsChatId();
        Long reviewRequestId = Long.parseLong(callbackData.split(" ")[2]);
        ReviewRequest reviewRequest = reviewRequestRepository.findById(reviewRequestId).orElseThrow(InvalidParameterException::new);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        LocalDate reviewRequestDate = reviewRequest.getDate();
        reviewRequest.getTimeSlots().
                stream().
                filter(timeSlot -> isTimeSlotTakenByAllMentors(timeSlot, reviewRequestDate)).
                sorted(Integer::compareTo).
                forEach(x -> {
                    List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    if (x == MIDNIGHT) {
                        inlineKeyboardButton.setText("00" + ":00 (" + reviewRequestDate.plusDays(1).format(defaultDateFormatter()) + ")");
                    } else {
                        inlineKeyboardButton.setText(x + ":00");
                    }
                    inlineKeyboardButton.setCallbackData(CallBack.APPROVE_REVIEW_REQUEST.getAlias() + " " + reviewRequest.getId() + " " + x);

                    keyboardButtonRow.add(inlineKeyboardButton);
                    rowList.add(keyboardButtonRow);
                });

        List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Отменить");
        inlineKeyboardButton.setCallbackData(CallBack.DENY_REVIEW_REQUEST.getAlias() + " " + reviewRequest.getId());

        keyboardButtonRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonRow);

        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage message = new SendMessage();
        message.setChatId(mentorsChatId);


        message.setText("@" + reviewRequest.getStudentUserName() + "\n" + reviewRequest.getTitle() + "\n" +
                reviewRequest.getDate().format(defaultDateFormatter()) + "\n");
        message.setReplyMarkup(inlineKeyboardMarkup);

        execute(message);
        reviewRequestRepository.save(reviewRequest);
    }

    private boolean isTimeSlotTakenByAllMentors(Integer timeSlot, LocalDate reviewRequestDate) {
        boolean isNotTakenByAllMentors = true;
        List<UserInfo> allMentors = userInfoRepository.findAllByUserType(UserType.MENTOR);
        for (UserInfo mentor : allMentors) {
            if (timeSlot == MIDNIGHT) {
                isNotTakenByAllMentors = !reviewRequestRepository.existsByBookedDateTimeAndMentorUserName(LocalDateTime.of(reviewRequestDate.plusDays(1), LocalTime.of(0, 0)), mentor.getUserName());
            } else {
                isNotTakenByAllMentors = !reviewRequestRepository.existsByBookedDateTimeAndMentorUserName(LocalDateTime.of(reviewRequestDate, LocalTime.of(timeSlot, 0)), mentor.getUserName());
            }
        }
        return isNotTakenByAllMentors;
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
        editMessageText.setMessageId(getMessageId(chatType, chatMessage));

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            if (isMessageNotFound(e)) {
                Message newMessage = execute(message);
                updateChatMessageId(chatType, chatMessage, newMessage.getMessageId());
                chatMessageRepository.save(chatMessage);
            } else {
                log.error("Ошибка при обновлении текста сообщения {}", e.getMessage(), e);
            }
        }
    }

    private int getMessageId(ChatType chatType, ChatMessage chatMessage) {
        if (chatType == ChatType.REPORTS_CHAT) {
            return chatMessage.getReportChatBotMessageId();
        } else {
            if (chatType != ChatType.USER_CHAT) {
                log.error("Был передан неподдерживаемый тип чата при обновлении текста сообщения: {}", chatType);
            }
            return chatMessage.getUserChatBotMessageId();
        }
    }

    private boolean isMessageNotFound(TelegramApiException e) {
        return e.getMessage().contains("message to edit not found") || e.getMessage().contains("MessageId parameter can't be empty");
    }

    private void updateChatMessageId(ChatType chatType, ChatMessage chatMessage, int newMessageId) {
        if (chatType == ChatType.REPORTS_CHAT) {
            chatMessage.setReportChatBotMessageId(newMessageId);
        } else if (chatType == ChatType.USER_CHAT) {
            chatMessage.setUserChatBotMessageId(newMessageId);
        }
    }

    private static boolean isReportUpdated(SendMessage message) {
        return message.getText().contains("Отчёт обновлен");
    }

    private static boolean isNewOrEditedReport(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REPORT_DATE_TIME.getAlias()) && !callbackAlias.equalsIgnoreCase(CallBack.DENY_REPORT_DATE_TIME.getAlias());
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
