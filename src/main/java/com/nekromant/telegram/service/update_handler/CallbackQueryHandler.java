package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.*;
import com.nekromant.telegram.service.update_handler.callback_strategy.CallbackStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.utils.DeleteMessageFactory;
import com.nekromant.telegram.utils.EditMessageReplyMarkupFactory;
import com.nekromant.telegram.utils.EditMessageTextFactory;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


import static com.nekromant.telegram.contants.MessageContants.REVIEW_BOOKED;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;

@Slf4j
@Component
public class CallbackQueryHandler {

    @Value("${owner.userName}")
    private String ownerUserName;

    private static final Integer MIDNIGHT = 24;

    private final Map<CallBack, CallbackStrategy> callbackStrategyMap;

    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private SpecialChatService specialChatService;
    @Autowired
    private ReviewRequestService reviewRequestService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private DeleteMessageFactory deleteMessageFactory;
    @Autowired
    private EditMessageReplyMarkupFactory editMessageReplyMarkupFactory;
    @Autowired
    private EditMessageTextFactory editMessageTextFactory;

    @Autowired
    public CallbackQueryHandler(List<CallbackStrategy> callbackStrategies) {
        this.callbackStrategyMap = callbackStrategies.stream()
                .collect(Collectors.toMap(CallbackStrategy::getPrefix, Function.identity()));
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Message callbackMessage = callbackQuery.getMessage();
        Map<ChatType, SendMessage> messageByChatTypeMap = getMessageByChatTypeMap(callbackMessage);
        DeleteMessageStrategy deleteMessageStrategy = new DeleteMessageStrategy();

        CallbackStrategy strategy = getCallbackStrategy(callbackData);
        strategy.executeCallbackQuery(callbackQuery, messageByChatTypeMap, deleteMessageStrategy);

        deleteReplyMessage(callbackMessage, deleteMessageStrategy.getMessagePart());
        sendMessagesIfNotEmpty(messageByChatTypeMap, callbackData);
    }

    private Map<ChatType, SendMessage> getMessageByChatTypeMap(Message callbackMessage) {
        Map<ChatType, SendMessage> sendMessageMap = new HashMap<>();
        sendMessageMap.put(ChatType.USER_CHAT, sendMessageFactory.createFromCallbackQuery(callbackMessage, ChatType.USER_CHAT));
        sendMessageMap.put(ChatType.MENTORS_CHAT, sendMessageFactory.createFromCallbackQuery(callbackMessage, ChatType.MENTORS_CHAT));
        sendMessageMap.put(ChatType.REPORTS_CHAT, sendMessageFactory.createFromCallbackQuery(callbackMessage, ChatType.REPORTS_CHAT));
        return sendMessageMap;
    }

    private CallbackStrategy getCallbackStrategy(String callbackData) {
        return callbackStrategyMap.get(CallBack.from(callbackData.split(" ")[0]));
    }

    private void deleteReplyMessage(Message callbackMessage, MessagePart messagePart) {
        switch (messagePart) {
            case MARKUP:
                deleteMessageMarkUp(callbackMessage);
                break;
            case ENTIRE_MESSAGE:
                deleteCallbackMessage(callbackMessage);
                break;
            default:
                log.error("Unsupported delete message strategy: {}", messagePart.name());
                throw new RuntimeException("Unsupported delete message strategy: " + messagePart.name());
        }
    }

    private void deleteCallbackMessage(Message callbackMessage) {
        try {
            sendMessageService.sendMessage(deleteMessageFactory.createFromCallbackMessage(callbackMessage));
        } catch (TelegramApiException e) {
            log.error("Failed to delete callback message (message id: {}): {}", callbackMessage.getMessageId(), e.getMessage(), e);
        }
    }

    private void deleteMessageMarkUp(Message callbackMessage) {
        try {
            sendMessageService.sendMessage(editMessageReplyMarkupFactory.createFromCallbackMessage(callbackMessage));
        } catch (TelegramApiException e) {
            log.error("Failed to delete message markup (message id: {}): {}", callbackMessage.getMessageId(), e.getMessage(), e);
        }
    }

    private void sendMessagesIfNotEmpty(Map<ChatType, SendMessage> messages, String callbackData) {
        String callbackAlias = callbackData.split(" ")[0];

        messages.forEach((chatType, message) -> {
            if (isNotEmptyMessage(message)) {
                try {
                    if (isNewOrEditedReport(callbackAlias)) {
                        handleNewOrEditedReport(chatType, message, callbackData);
                    } else if (isReviewRequest(callbackAlias)) {
                        handleReviewRequest(message, callbackData);
                    } else {
                        sendMessageService.sendMessage(message);
                    }
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке сообщения для чата {} (chat id: {}) {}", chatType, message.getChatId(), e.getMessage(), e);
                }
            }
        });
    }

    private void handleReviewRequest(SendMessage message, String callbackData) throws TelegramApiException {
        sendMessageService.sendMessage(message);
        if (isNotDenyForReviewRequest(callbackData)) {
            writeMentors(callbackData);
        }
    }

    private void handleNewOrEditedReport(ChatType chatType, SendMessage message, String callbackData) throws TelegramApiException {
        Integer messageId = extractMessageIdFromCallbackData(callbackData);
        ChatMessage chatMessage = chatMessageService.findChatMessageByUserMessageId(messageId);

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

    private static boolean isNotEmptyMessage(SendMessage sendMessage) {
        return sendMessage.getText() != null && !sendMessage.getText().isEmpty();
    }

    private static boolean isNewOrEditedReport(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REPORT_DATE_TIME.getAlias()) && !callbackAlias.equalsIgnoreCase(CallBack.DENY_REPORT_DATE_TIME.getAlias());
    }

    private boolean isReviewRequest(String callbackAlias) {
        return callbackAlias.equalsIgnoreCase(CallBack.SET_REVIEW_REQUEST_DATE_TIME.getAlias())
                || callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
    }

    private static boolean isNotDenyForReviewRequest(String callbackData) {
        String callbackAlias = callbackData.split(" ")[0];
        return !callbackAlias.equalsIgnoreCase(CallBack.DENY_REVIEW_REQUEST_DATE_TIME.getAlias());
    }

    private void writeMentors(String callbackData) throws TelegramApiException {
        String mentorsChatId = specialChatService.getMentorsChatId();
        Long reviewRequestId = Long.parseLong(callbackData.split(" ")[2]);
        ReviewRequest reviewRequest = reviewRequestService.findReviewRequestById(reviewRequestId);

        if (reviewRequest.getStudentInfo().getUserName().equals(ownerUserName)) {
            autoApproved(reviewRequest);
        } else {


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


            message.setText("@" + reviewRequest.getStudentInfo().getUserName() + "\n" + reviewRequest.getTitle() + "\n" +
                    reviewRequest.getDate().format(defaultDateFormatter()) + "\n");
            message.setReplyMarkup(inlineKeyboardMarkup);

            sendMessageService.sendMessage(message);
            reviewRequestService.save(reviewRequest);
        }
    }

    private void autoApproved(ReviewRequest reviewRequest) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String chatId = reviewRequest.getStudentInfo().getChatId().toString();
        sendMessage.setChatId(chatId);

        if (reviewRequest.getTimeSlots().size() != 1) {
            sendMessage.setText("Ошибка: Для данного ревью должен быть один таймслот");
            sendMessageService.sendMessage(sendMessage);
            return;
        }

        int timeSlot = reviewRequest.getTimeSlots().iterator().next();
        LocalDateTime timeSlotDateTime = (timeSlot == MIDNIGHT)
                ? LocalDateTime.of(reviewRequest.getDate().plusDays(1), LocalTime.of(0, 0))
                : LocalDateTime.of(reviewRequest.getDate(), LocalTime.of(timeSlot, 0));

        UserInfo mentorInfo = userInfoService.getUserInfo(reviewRequest.getStudentInfo().getChatId());

        if (reviewRequestService.existsByBookedDateTimeAndMentorUserInfo(timeSlotDateTime, mentorInfo)) {
            sendMessage.setText("Данное время у тебя уже занято");
        } else {
            reviewRequest.setBookedDateTime(timeSlotDateTime);
            reviewRequest.setMentorInfo(reviewRequest.getStudentInfo());
            reviewRequestService.save(reviewRequest);

            sendMessage.setText(String.format(REVIEW_BOOKED,
                    reviewRequest.getMentorInfo().getUserName(),
                    reviewRequest.getStudentInfo().getUserName(),
                    reviewRequest.getBookedDateTime().format(defaultDateTimeFormatter()))
            );
        }

        sendMessageService.sendMessage(sendMessage);
    }


    private Integer extractMessageIdFromCallbackData(String callbackData) {
        return Integer.parseInt(callbackData.split(" ")[3]);
    }

    private static boolean isReportUpdated(SendMessage message) {
        return message.getText().toLowerCase().contains("Отчёт обновлен".toLowerCase());
    }

    private void updateReportText(ChatType chatType, SendMessage message, ChatMessage chatMessage) throws TelegramApiException {
        try {
            sendMessageService.sendMessage(editMessageTextFactory.create(message.getChatId(), getMessageId(chatType, chatMessage), message.getText()));
        } catch (TelegramApiException e) {
            if (isMessageNotFound(e)) {
                Message newMessage = sendMessageService.sendMessage(message);
                updateChatMessageId(chatType, chatMessage, newMessage.getMessageId());
                chatMessageService.save(chatMessage);
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

        chatMessageService.save(chatMessage);
    }

    private boolean isTimeSlotTakenByAllMentors(Integer timeSlot, LocalDate reviewRequestDate) {
        boolean isNotTakenByAllMentors = true;
        List<UserInfo> allMentors = userInfoService.findAllMentors();
        for (UserInfo mentor : allMentors) {
            if (timeSlot == MIDNIGHT) {
                isNotTakenByAllMentors = !reviewRequestService.existsByBookedDateTimeAndMentorUserInfo(LocalDateTime.of(reviewRequestDate.plusDays(1), LocalTime.of(0, 0)), mentor);
            } else {
                isNotTakenByAllMentors = !reviewRequestService.existsByBookedDateTimeAndMentorUserInfo(LocalDateTime.of(reviewRequestDate, LocalTime.of(timeSlot, 0)), mentor);
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
