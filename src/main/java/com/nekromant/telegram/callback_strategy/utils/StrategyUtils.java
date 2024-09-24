package com.nekromant.telegram.callback_strategy.utils;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.security.InvalidParameterException;
import java.time.LocalDate;

import static com.nekromant.telegram.contants.MessageContants.TOO_MANY_REPORTS;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateFormatter;

@Component
public class StrategyUtils {
    @Autowired
    private ReviewRequestRepository reviewRequestRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private SpecialChatService specialChatService;

    public ReviewRequest getReviewRequest(Long reviewId) {
        return reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
    }

    public void setReportDate(Update update, String callbackData, SendMessage messageForUser, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        String date = callbackData.split(" ")[1];
        LocalDate localDate = LocalDate.parse(date, defaultDateFormatter());

        Report report = getReport(callbackData);

        setReportDateAndSave(messageForUser, messageForReportsChat, update, localDate, report);
        deleteMessageStrategy.setDeleteMessageStrategy(DeleteMessageStrategy.ENTIRE_MESSAGE);
    }

    private Report getReport(String callbackData) {
        Long reportId = Long.parseLong(callbackData.split(" ")[2]);
        return reportRepository.findById(reportId).orElseThrow(InvalidParameterException::new);
    }

    private void setReportDateAndSave(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, LocalDate localDate, Report report) {
        report.setDate(localDate);
        validateAndSaveReportDate(messageForUser, messageForReportsChat, update, report);
    }

    private void validateAndSaveReportDate(SendMessage messageForUser, SendMessage messageForReportsChat, Update update, Report report) {
        if (reportRepository.existsReportByDateAndStudentUserName(report.getDate(), report.getStudentUserName())) {
            messageForUser.setText(TOO_MANY_REPORTS);
            reportRepository.deleteById(report.getId());
        }  else {
            if (!isUserChatEqualsReportsChat(update)) {
                setMessageTextForReportsChat(messageForReportsChat, report);
            }
            setMessageTextReportDone(messageForUser, report);
            reportRepository.save(report);
        }
    }

    private void setMessageTextReportDone(SendMessage messageForUser, Report report) {
        messageForUser.setText("@" + report.getStudentUserName() + "\n" + report.getDate().format(defaultDateFormatter()) + "\n" + report.getHours() +
                "\n" + report.getTitle());
    }

    private boolean isUserChatEqualsReportsChat(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString().equals(specialChatService.getReportsChatId());
    }

    private void setMessageTextForReportsChat(SendMessage messageTextForReportsChat, Report report) {
        messageTextForReportsChat.setText("@" + report.getStudentUserName() + "\n" + report.getDate().format(defaultDateFormatter()) + "\n" + report.getHours() +
                "\n" + report.getTitle());
    }
}
