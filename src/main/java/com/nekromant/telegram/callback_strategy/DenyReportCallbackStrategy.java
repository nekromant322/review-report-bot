package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class DenyReportCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;

    @Override
    public void executeCallbackQuery(Update update, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        String callbackData = update.getCallbackQuery().getData();
        Long reportId = Long.parseLong(callbackData.split(" ")[1]);

        setMessageForUser(update, messageForUser);
        deleteReport(reportId);
        deleteMessageStrategy.setDeleteMessageStrategy(DeleteMessageStrategy.ENTIRE_MESSAGE);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.DENY_REPORT;
    }

    private void deleteReport(Long reportId) {
        reportRepository.findById(reportId).ifPresent(report -> reportRepository.deleteById(reportId));
    }

    private void setMessageForUser(Update update, SendMessage messageForUser) {
        messageForUser.setChatId(getChatIdFromUpdate(update));
        messageForUser.setText("Отправка отчёта отменена");
    }

    private String getChatIdFromUpdate(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString();
    }
}
