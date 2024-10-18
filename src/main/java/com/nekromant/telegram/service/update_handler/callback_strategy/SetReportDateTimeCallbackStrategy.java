package com.nekromant.telegram.service.update_handler.callback_strategy;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Slf4j
@Component
public class SetReportDateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private SpecialChatService specialChatService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void executeCallbackQuery(CallbackQuery callbackQuery, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);
        SendMessage messageForReportsChat = messageMap.get(ChatType.REPORTS_CHAT);

        setReportDate(callbackQuery, messageForUser, messageForReportsChat, deleteMessageStrategy);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.SET_REPORT_DATE_TIME;
    }

    private void setReportDate(CallbackQuery callbackQuery, SendMessage messageForUser, SendMessage messageForReportsChat, DeleteMessageStrategy deleteMessageStrategy) {
        String callbackData = callbackQuery.getData();
        String[] dataParts = callbackData.split(" ");
        String date = dataParts[1];
        Long reportId = Long.parseLong(callbackData.split(" ")[2]);
        LocalDate localDate = LocalDate.parse(date, defaultDateFormatter());
        Integer messageId = Integer.parseInt(callbackData.split(" ")[3]);

        Report updatedReport = getUpdatedTemporaryReport(reportId);
        updatedReport.setDate(localDate);

        setReportDateAndSave(messageForUser, messageForReportsChat, callbackQuery, updatedReport, messageId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    private Report getUpdatedTemporaryReport(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(InvalidParameterException::new);
    }

    private void setReportDateAndSave(SendMessage messageForUser, SendMessage messageForReportsChat, CallbackQuery callbackQuery, Report updatedReport, Integer messageId) {
        ChatMessage currentMessage = chatMessageRepository.findByUserMessageId(messageId);

        if (currentMessage != null) {
            log.info("Редактирование. Сообщение (message id: {}) существует в БД", messageId);
            handleExistingMessage(messageForUser, messageForReportsChat, updatedReport, currentMessage);
        } else {
            log.info("Новое сообщение. Сообщения (message id: {}) не существует в БД", messageId);
            handleNewMessage(messageForUser, messageForReportsChat, callbackQuery, updatedReport, messageId);
        }
    }

    private void handleExistingMessage(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, ChatMessage currentMessage) {
        if (currentMessage.getReport() == null) {
            log.info("К текущему сообщению (message id: {}) не привязан отчёт", currentMessage.getUserMessageId());
            handleNewReportForExistingMessage(messageForUser, messageForReportsChat, updatedReport, currentMessage);
        } else {
            log.error("К текущему сообщению  (message id: {}) привязан отчёт. Ошибка в логике исполнения программы, обработка должна быть произведена в методе MentoringReviewBot.processEditedMessageUpdate().", currentMessage.getUserMessageId());
            messageForUser.setText("Ошибка в логике исполнения программы. Обратитесь к разработчику.");
        }
    }

    private void handleNewReportForExistingMessage(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, ChatMessage currentMessage) {
        if (reportRepository.existsReportByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName())) {
            List<Report> existingReportsOnReceivedDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
            Report reportLikeReceived = existingReportsOnReceivedDate.get(0);
            log.info("Отчёт (report id: {}) с новой датой ({}) существует (обновить его и привязать к текущему сообщению)", reportLikeReceived.getId(), updatedReport.getDate().format(defaultDateFormatter()));
            deleteDuplicateReports(existingReportsOnReceivedDate);
            updateReportFromAnotherReport(updatedReport, reportLikeReceived);
            currentMessage.setReport(reportLikeReceived);
            chatMessageRepository.save(currentMessage);

            deleteNewTemporaryReport(updatedReport);

            setMessageTextReportIsUpdated(messageForUser, updatedReport);
            setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
        } else {
            log.info("Отчёта с новой датой ({}) не существует (новый отчёт и привязать к текущему сообщению)", updatedReport.getDate().format(defaultDateFormatter()));

            Report savedReport = reportRepository.save(updatedReport);
            currentMessage.setReport(savedReport);
            chatMessageRepository.save(currentMessage);

            setMessageTextForUserReportDone(messageForUser, updatedReport);
            setMessageTextForReportsChatReportDone(messageForReportsChat, updatedReport);
        }
    }

    private void handleNewMessage(SendMessage messageForUser, SendMessage messageForReportsChat, CallbackQuery callbackQuery, Report updatedReport, Integer messageId) {
        if (reportRepository.existsReportByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName())) {
            List<Report> existingReportsOnReceivedDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
            Report reportLikeReceived = existingReportsOnReceivedDate.get(0);
            log.info("Отчёт с такой датой ({}) существует (report id: {}, обновить его)", updatedReport.getDate().format(defaultDateFormatter()), reportLikeReceived.getId());
            deleteDuplicateReports(existingReportsOnReceivedDate);

            updateReportFromAnotherReport(updatedReport, reportLikeReceived);
            log.info("Старый отчёт обновлён (report id: {})", reportLikeReceived.getId());

            ChatMessage oldMessage = chatMessageRepository.findChatMessageByReport(reportLikeReceived);

            if (oldMessage != null && oldMessage.getReportChatBotMessageId() != null) {
                log.info("К старому отчёту ({}) уже было привязано сообщение в БД (chatMessage: {})", reportLikeReceived, oldMessage);
                oldMessage.setReport(reportLikeReceived);
                oldMessage.setUserMessageId(messageId);
                log.info("К сообщению в БД привязан старый отчёт(с обновлёнными данными) и сообщение-отчёт от пользователя");

                chatMessageRepository.save(oldMessage);

                deleteNewTemporaryReport(updatedReport);

                messageForUser.setText(String.format("Вы обновили существующий отчёт за %s:\n@%s\n%s\n%s\n%s",
                        reportLikeReceived.getDate().format(defaultDateFormatter()),
                        updatedReport.getStudentUserName(),
                        updatedReport.getDate().format(defaultDateFormatter()),
                        updatedReport.getHours(),
                        updatedReport.getTitle()
                ));
                setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
            } else if (oldMessage == null) {
                log.info("К старому отчёту (report id: {}) не было привязано сообщение в БД", reportLikeReceived.getId());

                ChatMessage newMessage = ChatMessage.builder()
                        .report(reportLikeReceived)
                        .userMessageId(messageId)
                        .build();
                chatMessageRepository.save(newMessage);
                log.info("К старому отчёту  (report id: {}) теперь привязано сообщение в БД (chatMessage: {})", reportLikeReceived.getId(), newMessage.getId());

                deleteNewTemporaryReport(updatedReport);

                messageForUser.setText(String.format("Вы обновили существующий отчёт за %s:\n@%s\n%s\n%s\n%s",
                        reportLikeReceived.getDate().format(defaultDateFormatter()),
                        updatedReport.getStudentUserName(),
                        updatedReport.getDate().format(defaultDateFormatter()),
                        updatedReport.getHours(),
                        updatedReport.getTitle()
                ));
                messageForReportsChat.setText(String.format("Пользователь обновил существующий отчёт за %s:\n@%s\n%s\n%s\n%s",
                        reportLikeReceived.getDate().format(defaultDateFormatter()),
                        updatedReport.getStudentUserName(),
                        updatedReport.getDate().format(defaultDateFormatter()),
                        updatedReport.getHours(),
                        updatedReport.getTitle()
                ));
            }
        } else {
            log.info("Отчёта с новой датой ({}) не существует (сохранить новый отчёт и новое сообщение в БД)", updatedReport.getDate().format(defaultDateFormatter()));
            saveNewReport(messageForUser, messageForReportsChat, callbackQuery, updatedReport, messageId);

            setMessageTextForUserReportDone(messageForUser, updatedReport);
            setMessageTextForReportsChatReportDone(messageForReportsChat, updatedReport);
        }
    }

    private void deleteDuplicateReports(List<Report> existingReports) {
        existingReports.stream()
                .skip(1)
                .forEach(report -> {
                    chatMessageRepository.deleteChatMessageByReport(report);
                    reportRepository.delete(report);
                });
    }

    private void updateReportFromAnotherReport(Report reportFrom, Report reportForUpdate) {
        reportForUpdate.setHours(reportFrom.getHours());
        reportForUpdate.setTitle(reportFrom.getTitle());
        reportForUpdate.setDate(reportFrom.getDate());
        reportRepository.save(reportForUpdate);
    }

    private void deleteNewTemporaryReport(Report report) {
        reportRepository.findById(report.getId()).ifPresent(reportRepository::delete);
    }

    private void saveNewReport(SendMessage messageForUser, SendMessage messageForReportsChat, CallbackQuery callbackQuery, Report report, Integer messageId) {
        if (!isUserChatEqualsReportsChat(callbackQuery)) {
            setMessageTextForReportsChatReportDone(messageForReportsChat, report);
        }
        setMessageTextForUserReportDone(messageForUser, report);
        Report savedReport = reportRepository.save(report);
        ChatMessage chatMessage = ChatMessage.builder()
                        .userMessageId(messageId)
                        .report(savedReport)
                        .build();
        chatMessageRepository.save(chatMessage);
    }

    private boolean isUserChatEqualsReportsChat(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getChatId().toString().equals(specialChatService.getReportsChatId());
    }

    private void setMessageTextForUserReportDone(SendMessage messageForUser, Report report) {
        messageForUser.setText(String.format("@%s\n%s\n%s\n%s",
                report.getStudentUserName(),
                report.getDate().format(defaultDateFormatter()),
                report.getHours(),
                report.getTitle()));
    }

    private void setMessageTextForReportsChatReportDone(SendMessage messageTextForReportsChat, Report report) {
        messageTextForReportsChat.setText(String.format("@%s\n%s\n%s\n%s",
                report.getStudentUserName(),
                report.getDate().format(defaultDateFormatter()),
                report.getHours(),
                report.getTitle()));
    }

    private static void setMessageTextReportIsUpdated(SendMessage sendMessage, Report updatedReport) {
        sendMessage.setText(String.format("Отчёт обновлен %s:\n@%s\n%s\n%s\n%s",
                LocalDate.now().format(defaultDateFormatter()),
                updatedReport.getStudentUserName(),
                updatedReport.getDate().format(defaultDateFormatter()),
                updatedReport.getHours(),
                updatedReport.getTitle()
        ));
    }
}
