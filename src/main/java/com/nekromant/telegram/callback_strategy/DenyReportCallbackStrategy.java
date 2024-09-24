package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategyComponent;
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
    public void executeCallbackQuery(Update update, String callbackData, SendMessage messageForUser, SendMessage messageForMentors, SendMessage messageForReportsChat, DeleteMessageStrategyComponent deleteMessageStrategy) {
        Long reportId = Long.parseLong(callbackData.split(" ")[1]);

        setChatIdForUser(update, messageForUser);
        messageForUser.setText("Отправка отчёта отменена");
        reportRepository.deleteById(reportId);
        deleteMessageStrategy.setDeleteMessageStrategy(DeleteMessageStrategy.ENTIRE_MESSAGE);
    }

    private void setChatIdForUser(Update update, SendMessage messageForUser) {
        messageForUser.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
    }
}
