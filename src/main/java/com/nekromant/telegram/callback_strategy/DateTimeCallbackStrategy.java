package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.model.ChatMessage;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Slf4j
@Component
public class DateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private SpecialChatService specialChatService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);
        SendMessage messageForReportsChat = messageMap.get(ChatType.REPORTS_CHAT);

        setReportDate(update, messageForUser, messageForReportsChat, deleteMessageStrategy);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.SET_REPORT_DATE_TIME;
    }

    private void setReportDate(Update update, SendMessage messageForUser, SendMessage messageForReportsChat, DeleteMessageStrategy deleteMessageStrategy) {
        String callbackData = update.getCallbackQuery().getData();
        String[] dataParts = callbackData.split(" ");
        String date = dataParts[1];
        Long reportId = Long.parseLong(callbackData.split(" ")[2]);
        LocalDate localDate = LocalDate.parse(date, defaultDateFormatter());
        Integer messageId = Integer.parseInt(callbackData.split(" ")[3]);

        Report updatedReport = getUpdatedTemporaryReport(reportId);
        updatedReport.setDate(localDate);

        setReportDateAndSave(messageForUser, messageForReportsChat, update, updatedReport, messageId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    private Report getUpdatedTemporaryReport(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(InvalidParameterException::new);
    }

    private void setReportDateAndSave(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report updatedReport, Integer messageId) {
        ChatMessage currentMessage = chatMessageRepository.findByUserMessageId(messageId);

        if (currentMessage != null) {
            log.debug("Редактирование. Сообщение существует в БД");
            handleExistingMessage(messageForUser, messageForReportsChat, updatedReport, currentMessage);
        } else {
            log.debug("Новое сообщение. Сообщения не существует в БД");
            handleNewMessage(messageForUser, messageForReportsChat, update, updatedReport, messageId);
        }
    }

    private void handleExistingMessage(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, ChatMessage currentMessage) {
        if (currentMessage.getReport() != null) {
            log.debug("К текущему сообщению привязан отчёт");
            handleExistingReport(messageForUser, messageForReportsChat, updatedReport, currentMessage);
        } else {
            log.debug("К текущему сообщению не привязан отчёт");
            handleNewReportForExistingMessage(messageForUser, messageForReportsChat, updatedReport, currentMessage);
        }
    }

    private void handleExistingReport(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, ChatMessage currentMessage) {
        Report currentMessageReport = currentMessage.getReport();

        if (reportRepository.existsReportByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName())) {
            log.debug("Отчёт с новой датой существует");
            if (isExistingReportEqualsReportFromCurrentMessage(currentMessage, updatedReport)) {
                log.debug("Это отчёт, привязанный к текущему сообщению (обновить текущий отчёт)");
                updateReportFromAnotherReport(updatedReport, currentMessageReport);

                deleteNewTemporaryReport(updatedReport);

                setMessageTextReportIsUpdated(messageForUser, updatedReport);
                setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
            } else {
                log.debug("Это НЕ отчёт, привязанный к текущему сообщению (Написать юзеру, чтобы редактировал соответствующее сообщение)");

                deleteNewTemporaryReport(updatedReport);

                messageForUser.setText("Отчёт-сообщение с такой датой (" + defaultDateFormatter().format(updatedReport.getDate()) + ") уже есть. Отредактируйте его или выберите другую дату.");
            }
        } else {
            log.debug("Отчёта с новой датой не существует (обновить отчёт привязанный к текущему сообщению)");
            updateReportFromAnotherReport(updatedReport, currentMessageReport);

            deleteNewTemporaryReport(updatedReport);

            setMessageTextReportIsUpdated(messageForUser, updatedReport);
            setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
        }
    }

    private void handleNewReportForExistingMessage(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, ChatMessage currentMessage) {
        if (reportRepository.existsReportByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName())) {
            log.debug("Отчёт с новой датой существует (обновить его и привязать к текущему сообщению)");
            List<Report> existingReportsOnReceivedDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
            Report reportLikeReceived = existingReportsOnReceivedDate.get(0);
            deleteDuplicateReports(existingReportsOnReceivedDate);
            updateReportFromAnotherReport(updatedReport, reportLikeReceived);
            currentMessage.setReport(reportLikeReceived);
            chatMessageRepository.save(currentMessage);

            deleteNewTemporaryReport(updatedReport);

            setMessageTextReportIsUpdated(messageForUser, updatedReport);
            setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
        } else {
            log.debug("Отчёта с новой датой не существует (новый отчёт и привязать к текущему сообщению)");

            Report savedReport = reportRepository.save(updatedReport);
            currentMessage.setReport(savedReport);
            chatMessageRepository.save(currentMessage);

            deleteNewTemporaryReport(updatedReport);

            setMessageTextForUserReportDone(messageForUser, updatedReport);
            setMessageTextForReportsChatReportDone(messageForReportsChat, updatedReport);
        }
    }

    private void handleNewMessage(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report updatedReport, Integer messageId) {
        if (reportRepository.existsReportByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName())) {
            log.debug("Отчёт с новой датой существует (обновить его, удалить старое сообщение и сохранить новое сообщение с переданным айди в БД с этим отчётом)");
            List<Report> existingReportsOnReceivedDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
            Report reportLikeReceived = existingReportsOnReceivedDate.get(0);
            deleteDuplicateReports(existingReportsOnReceivedDate);
            updateReportFromAnotherReport(updatedReport, reportLikeReceived);

            ChatMessage oldMessage = chatMessageRepository.findChatMessageByReport(reportLikeReceived);

            ChatMessage newMessage = ChatMessage.builder()
                    .userMessageId(messageId)
                    .report(reportLikeReceived)
                    .build();
            if (oldMessage.getReportChatBotMessageId() != null) {
                newMessage.setReportChatBotMessageId(oldMessage.getReportChatBotMessageId());
            }
            chatMessageRepository.save(newMessage);
            chatMessageRepository.delete(oldMessage);

            deleteNewTemporaryReport(updatedReport);

            messageForUser.setText(String.format("Вы обновили существующий отчёт за %s:\n@%s\n%s\n%s\n%s",
                    LocalDate.now().format(defaultDateFormatter()),
                    updatedReport.getStudentUserName(),
                    updatedReport.getDate().format(defaultDateFormatter()),
                    updatedReport.getHours(),
                    updatedReport.getTitle()
            ));
            setMessageTextReportIsUpdated(messageForReportsChat, updatedReport);
        } else {
            log.debug("Отчёта с новой датой не существует (новый отчёт и сохранить новое сообщение с переданным айди в БД с этим отчётом)");
            saveNewReport(messageForUser, messageForReportsChat, update, updatedReport, messageId);

            setMessageTextForUserReportDone(messageForUser, updatedReport);
            setMessageTextForReportsChatReportDone(messageForReportsChat, updatedReport);
        }
    }

    private boolean isExistingReportEqualsReportFromCurrentMessage(ChatMessage chatMessage, Report updatedReport) {
        List<Report> existingReportsWithSuchDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
        Report reportLikeReceived = existingReportsWithSuchDate.get(0);
        deleteDuplicateReports(existingReportsWithSuchDate);
        return chatMessage.getReport().equals(reportLikeReceived);
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
        reportRepository.deleteById(report.getId());
    }

    private void saveNewReport(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report, Integer messageId) {
        if (!isUserChatEqualsReportsChat(update)) {
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

    private boolean isUserChatEqualsReportsChat(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString().equals(specialChatService.getReportsChatId());
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
