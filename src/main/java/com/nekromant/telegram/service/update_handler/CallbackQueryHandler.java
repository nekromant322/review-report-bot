package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.repository.UserInfoRepository;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.update_handler.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
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

import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Slf4j
@Component
public class CallbackQueryHandler {

    private static final Integer MIDNIGHT = 24;

    private final Map<CallBack, CallbackStrategy> callbackStrategyMap;

    private final SendMessageFactory sendMessageFactory;
    private final ChatMessageRepository chatMessageRepository; // TODO replace with service
    private final SpecialChatService specialChatService;
    private final ReviewRequestRepository reviewRequestRepository; // TODO replace with service
    private final UserInfoRepository userInfoRepository; // TODO replace with service

    private final SendMessageService sendMessageService;

    @Autowired
    public CallbackQueryHandler(List<CallbackStrategy> callbackStrategies,
                                SendMessageService sendMessageService,
                                SendMessageFactory sendMessageFactory,
                                ChatMessageRepository chatMessageRepository,
                                SpecialChatService specialChatService,
                                ReviewRequestRepository reviewRequestRepository,
                                UserInfoRepository userInfoRepository) {
        this.callbackStrategyMap = callbackStrategies.stream()
                .collect(Collectors.toMap(CallbackStrategy::getPrefix, Function.identity()));
        this.sendMessageService = sendMessageService;
        this.sendMessageFactory = sendMessageFactory;
        this.chatMessageRepository = chatMessageRepository;
        this.specialChatService = specialChatService;
        this.reviewRequestRepository = reviewRequestRepository;
        this.userInfoRepository = userInfoRepository;
    }

    // TODO развернуть логику, разбить на отдельные сценарии
    public void handleCallbackQuery(CallbackQuery callbackQuery) {

        Map<ChatType, SendMessage> messageByChatTypeMap = getMessageByChatTypeMap(callbackQuery);
        String callbackData = callbackQuery.getData();

        CallbackStrategy strategy = getCallbackStrategy(callbackData);
        DeleteMessageStrategy deleteMessageStrategy = new DeleteMessageStrategy();
        strategy.executeCallbackQuery(callbackQuery, messageByChatTypeMap, deleteMessageStrategy);

        deleteReplyMessage(callbackQuery, deleteMessageStrategy.getMessagePart());

        sendMessagesIfNotEmpty(messageByChatTypeMap, callbackData);
    }

    private Map<ChatType, SendMessage> getMessageByChatTypeMap(CallbackQuery callbackQuery) {
        Map<ChatType, SendMessage> sendMessageMap = new HashMap<>();
        sendMessageMap.put(ChatType.USER_CHAT, sendMessageFactory.createFromCallbackQuery(callbackQuery, ChatType.USER_CHAT));
        sendMessageMap.put(ChatType.MENTORS_CHAT, sendMessageFactory.createFromCallbackQuery(callbackQuery, ChatType.MENTORS_CHAT));
        sendMessageMap.put(ChatType.REPORTS_CHAT, sendMessageFactory.createFromCallbackQuery(callbackQuery, ChatType.REPORTS_CHAT));
        return sendMessageMap;
    }

    private CallbackStrategy getCallbackStrategy(String callbackData) {
        return callbackStrategyMap.get(CallBack.from(callbackData.split(" ")[0]));
    }

    private void deleteReplyMessage(CallbackQuery callbackQuery, MessagePart messagePart) {
        switch (messagePart) {
            case MARKUP:
                deleteMessageMarkUp(callbackQuery);
                break;
            case ENTIRE_MESSAGE:
                deleteCallbackMessage(callbackQuery);
                break;
            default:
                log.error("Unsupported delete message strategy: {}", messagePart.name());
                throw new RuntimeException("Unsupported delete message strategy: " + messagePart.name());
        }
    }

    private void deleteCallbackMessage(CallbackQuery callbackQuery) {
        // TODO replace with factory like SendMessageFactory
        DeleteMessage deleteMessage = new DeleteMessage();
        Message message = callbackQuery.getMessage();
        deleteMessage.setChatId(message.getChatId().toString());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            sendMessageService.sendMessage(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to delete callback message (message id: {}): {}", message.getMessageId(), e.getMessage(), e);
        }
    }

    private void deleteMessageMarkUp(CallbackQuery callbackQuery) {
        // TODO replace with factory like SendMessageFactory
        EditMessageReplyMarkup message = new EditMessageReplyMarkup();
        Message callbackMessage = callbackQuery.getMessage();
        message.setChatId(callbackMessage.getChatId().toString());
        message.setMessageId(callbackMessage.getMessageId());
        message.setReplyMarkup(null);
        try {
            sendMessageService.sendMessage(message);
        } catch (TelegramApiException e) {
            log.error("Failed to delete message markup (message id: {}): {}", callbackMessage.getMessageId(), e.getMessage(), e);
        }
    }

    private void sendMessagesIfNotEmpty(Map<ChatType, SendMessage> messages, String callbackData) {

        messages.forEach((chatType, message) -> {
//            ChatMessage chatMessage = chatMessageRepository.findByUserMessageId(extractMessageIdFromCallbackData(callbackData));
//            SendObject sendObject = new SendObject(chatType, message, chatMessage); // TODO заменить SendObject c полями ChatType, SendMessage, ChatMessage (создан уже)

            // TODO развернуть логику, разбить на отдельные сценарии? Несколько дней займёт, всё равно что с нуля писать весь MentoringReviewBot.class
            //  под каждый сценарий свой хендлер
            //  хэндлеры в список
            //  у хэндлеров должно быть поле, по которому мы будем искать нужны в списке стрим-фильтром
            //  метод хэндела возвращат <T extends Serializable, Method extends BotApiMethod<T>> List<Method> или просто Method
            //  https://javarush.com/en/groups/posts/en.2966.create-a-telegram-bot-using-spring-boot-pt2-quiz-bot
            //  https://github.com/whiskels/NotifierBot/blob/master/src/main/java/com/whiskels/notifier/infrastructure/admin/telegram/MessageProcessor.java
            if (isNotEmptyMessage(message)) {
                try {
                    String callbackAlias = callbackData.split(" ")[0];
                    if (isNewOrEditedReport(callbackAlias)) {
                        updateReportMessage(chatType, message, callbackData);
                    } else if (isReviewRequest(callbackAlias)) {
                        sendMessageService.sendMessage(message);
                        if (isNotDenyForReviewRequest(callbackAlias)) {
                            writeMentors(callbackData);
                        }
                    } else {
                        sendMessageService.sendMessage(message);
                    }
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке сообщения для чата {} (chat id: {}) {}", chatType, message.getChatId(), e.getMessage(), e);
                }
            }
        });
    }

    private static boolean isNotEmptyMessage(SendMessage sendMessage) {
        return sendMessage.getText() != null && !sendMessage.getText().isEmpty();
    }

    private static boolean isNewOrEditedReport(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REPORT_DATE_TIME.getAlias()) && !callbackAlias.equalsIgnoreCase(CallBack.DENY_REPORT_DATE_TIME.getAlias());
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
            sendMessageService.sendMessage(message);
        }
    }

    private boolean isReviewRequest(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REVIEW_REQUEST_DATE_TIME.getAlias())
                || callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
    }

    private static boolean isNotDenyForReviewRequest(String callbackAlias) {
        return !callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
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

        sendMessageService.sendMessage(message);
        reviewRequestRepository.save(reviewRequest);
    }

    private Integer extractMessageIdFromCallbackData(String callbackData) {
        return Integer.parseInt(callbackData.split(" ")[3]);
    }

    private static boolean isReportUpdated(SendMessage message) {
        return message.getText().toLowerCase().contains("Отчёт обновлен".toLowerCase());
    }

    private void updateReportText(ChatType chatType, SendMessage message, ChatMessage chatMessage) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(message.getText());
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(getMessageId(chatType, chatMessage));

        // TODO перенести в TelegramApiExceptionHandler
        try {
            sendMessageService.sendMessage(editMessageText);
        } catch (TelegramApiException e) {
            if (isMessageNotFound(e)) {
                Message newMessage = sendMessageService.sendMessage(message);
                updateChatMessageId(chatType, chatMessage, newMessage.getMessageId());
                chatMessageRepository.save(chatMessage);
            } else if (isMessageNotModified(e)) {
                log.info("Ни текст сообщения, ни разметка не были изменены в чате {}", chatType);
            } else {
                log.error("Ошибка при обновлении текста сообщения для чата {} (chat id: {}) {}", chatType, message.getChatId(), e.getMessage(), e);
            }
        }
    }

    private void sendNewReportSavedAndSetBotMessageId(ChatType chatType, SendMessage message, ChatMessage chatMessage) throws TelegramApiException {
        Message executedMessage = sendMessageService.sendMessage(message);

        if (chatType == ChatType.REPORTS_CHAT) {
            chatMessage.setReportChatBotMessageId(executedMessage.getMessageId());
        } else if (chatType == ChatType.USER_CHAT) {
            chatMessage.setUserChatBotMessageId(executedMessage.getMessageId());
        }

        chatMessageRepository.save(chatMessage);
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

    private boolean isMessageNotModified(TelegramApiException e) {
        return e.getMessage().contains("message is not modified");
    }
}
