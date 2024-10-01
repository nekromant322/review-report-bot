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

        Report report = getReport(reportId);

        setReportDateAndSave(messageForUser, messageForReportsChat, update, localDate, report, messageId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    private Report getReport(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(InvalidParameterException::new);
    }

    private void setReportDateAndSave(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, LocalDate localDate, Report report, Integer messageId) {
        report.setDate(localDate);
        validateAndSaveReportDate(messageForUser, messageForReportsChat, update, report, messageId);
    }

    private void validateAndSaveReportDate(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report, Integer messageId) {
        if (chatMessageRepository.findByUserMessageId(messageId) != null) {
            updateReportByMessageId(messageForUser, messageForReportsChat, report, messageId);
        } else if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
            deleteNewTemporaryReport(report);
            messageForUser.setText("Отчёт с такой датой уже есть. Отредактируйте отчёт с выбранной датой (" + defaultDateFormatter().format(report.getDate()) + ") или выберите другую дату");
        } else {
            saveNewReport(messageForUser, messageForReportsChat, update, report, messageId);
        }
    }

    private void updateReportByMessageId(SendMessage messageForUser, SendMessage messageForReportsChat, Report updatedReport, Integer messageId) {
        ChatMessage chatMessageOldReport = chatMessageRepository.findByUserMessageId(messageId);

        if (chatMessageOldReport == null) {
            messageForUser.setText("Редактируемое сообщение не было найдено в БД. Редактирование отчёта для данного сообщения невозможно");
        } else if (chatMessageOldReport.getReport() == null) {
            messageForUser.setText("Отчёт не был привязан к сообщению. Редактирование отчёта для данного сообщения невозможно");
        } else {
            Report reportByMessageId = chatMessageOldReport.getReport();

            if (isUpdateForExistingReportWithSameDate(messageId, updatedReport)) {
                updateReportByMessageId(updatedReport, reportByMessageId);

                messageForUser.setText(String.format("Отчёт обновлен %s:\n@%s\n%s\n%s\n%s",
                        LocalDate.now().format(defaultDateFormatter()),
                        updatedReport.getStudentUserName(),
                        updatedReport.getDate().format(defaultDateFormatter()),
                        updatedReport.getHours(),
                        updatedReport.getTitle()
                ));

                messageForReportsChat.setText(String.format("Отчёт обновлен %s:\n@%s\n%s\n%s\n%s",
                        LocalDate.now().format(defaultDateFormatter()),
                        updatedReport.getStudentUserName(),
                        updatedReport.getDate().format(defaultDateFormatter()),
                        updatedReport.getHours(),
                        updatedReport.getTitle()
                ));
            } else {
                messageForUser.setText("Отчёт с такой датой уже есть. Отредактируйте отчёт с выбранной датой (" + defaultDateFormatter().format(updatedReport.getDate()) + ") или выберите другую дату");
            }
            deleteNewTemporaryReport(updatedReport);
        }
    }

    private boolean isUpdateForExistingReportWithSameDate(Integer messageId, Report updatedReport) {
        List<Report> existingReportsWithSuchDate = reportRepository.findByDateAndStudentUserName(updatedReport.getDate(), updatedReport.getStudentUserName());
        Report reportLikeReceived = existingReportsWithSuchDate.get(0);
        deleteDuplicateReports(existingReportsWithSuchDate);
        ChatMessage chatMessageReportWithSameDate = chatMessageRepository.findChatMessageByReport(reportLikeReceived);
        return chatMessageReportWithSameDate.getUserMessageId().equals(messageId);
    }

    private void deleteDuplicateReports(List<Report> existingReports) {
        existingReports.stream()
                .skip(1)
                .forEach(report -> {
                    chatMessageRepository.deleteChatMessageByReport(report);
                    reportRepository.delete(report);
                });
    }

    private void updateReportByMessageId(Report report, Report reportByMessageId) {
        reportByMessageId.setHours(report.getHours());
        reportByMessageId.setTitle(report.getTitle());
        reportByMessageId.setDate(report.getDate());
        reportRepository.save(reportByMessageId);
    }

    private void deleteNewTemporaryReport(Report report) {
        reportRepository.deleteById(report.getId());
    }

    private void saveNewReport(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report, Integer messageId) {
        if (!isUserChatEqualsReportsChat(update)) {
            setMessageTextForReportsChat(messageForReportsChat, report);
        }
        setMessageTextReportDone(messageForUser, report);
        Report savedReport = reportRepository.save(report);
        ChatMessage chatMessage = ChatMessage.builder()
                        .userMessageId(messageId)
                        .report(savedReport)
                        .build();
        chatMessageRepository.save(chatMessage);
    }

    private void setMessageTextReportDone(SendMessage messageForUser, Report report) {
        messageForUser.setText(String.format("@%s\n%s\n%s\n%s", report.getStudentUserName(), report.getDate().format(defaultDateFormatter()), report.getHours(), report.getTitle()));
    }

    private boolean isUserChatEqualsReportsChat(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString().equals(specialChatService.getReportsChatId());
    }

    private void setMessageTextForReportsChat(SendMessage messageTextForReportsChat, Report report) {
        messageTextForReportsChat.setText(String.format("@%s\n%s\n%s\n%s", report.getStudentUserName(), report.getDate().format(defaultDateFormatter()), report.getHours(), report.getTitle()));
    }
}
