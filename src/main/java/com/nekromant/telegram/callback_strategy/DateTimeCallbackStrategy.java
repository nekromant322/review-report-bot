package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class DateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private SpecialChatService specialChatService;

    @Override
    public void executeCallbackQuery(Update update, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        setReportDate(update, messageForUser, messageForReportsChat, deleteMessageStrategy);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.DATE_TIME;
    }

    private void setReportDate(Update update, SendMessage messageForUser, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        String callbackData = update.getCallbackQuery().getData();
        String[] dataParts = callbackData.split(" ");
        String date = dataParts[1];
        Long reportId = Long.parseLong(callbackData.split(" ")[2]);
        LocalDate localDate = LocalDate.parse(date, defaultDateFormatter());

        Report report = getReport(reportId);

        setReportDateAndSave(messageForUser, messageForReportsChat, update, localDate, report);
        deleteMessageStrategy.setDeleteMessageStrategy(DeleteMessageStrategy.ENTIRE_MESSAGE);
    }

    private Report getReport(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(InvalidParameterException::new);
    }

    private void setReportDateAndSave(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, LocalDate localDate, Report report) {
        report.setDate(localDate);
        validateAndSaveReportDate(messageForUser, messageForReportsChat, update, report);
    }

    private void validateAndSaveReportDate(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report) {
        if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
            updateExistingReport(messageForUser, messageForReportsChat, report);
        } else {
            saveNewReport(messageForUser, messageForReportsChat, update, report);
        }
    }

    private void updateExistingReport(SendMessage messageForUser, SendMessage messageForReportsChat, Report report) {
        List<Report> existingReports = reportRepository.findByDateAndStudentUserName(report.getDate(), report.getStudentUserName());
        Report oldReport = existingReports.get(0);

        deleteDuplicateReports(existingReports);
        updateOldReport(report, oldReport);
        deleteNewTemporaryReport(report);

        messageForUser.setText(String.format("Отчёт обновлен:\nДата: %s\nЧасы: %s\nЗаголовок: %s",
                report.getDate().format(defaultDateFormatter()),
                report.getHours(),
                report.getTitle()
        ));
        messageForReportsChat.setText(String.format("Отчёт обновлен:\nДата: %s\nЧасы: %s\nЗаголовок: %s",
                report.getDate().format(defaultDateFormatter()),
                report.getHours(),
                report.getTitle()
        ));
    }

    private void deleteDuplicateReports(List<Report> existingReports) {
        existingReports.stream()
                .skip(1)
                .forEach(reportRepository::delete);
    }

    private void updateOldReport(Report report, Report oldReport) {
        oldReport.setHours(report.getHours());
        oldReport.setTitle(report.getTitle());
        reportRepository.save(oldReport);
    }

    private void deleteNewTemporaryReport(Report report) {
        reportRepository.deleteById(report.getId());
    }

    private void saveNewReport(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report) {
        if (!isUserChatEqualsReportsChat(update)) {
            setMessageTextForReportsChat(messageForReportsChat, report);
        }
        setMessageTextReportDone(messageForUser, report);
        reportRepository.save(report);
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
