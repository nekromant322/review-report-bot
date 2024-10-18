package com.nekromant.telegram.service.update_handler.callback_strategy;

import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.service.update_handler.callback_strategy.delete_message_strategy.MessagePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Component
public class DenyReportDateTimeCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public void executeCallbackQuery(CallbackQuery callbackQuery, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);

        String callbackData = callbackQuery.getData();
        Long reportId = Long.parseLong(callbackData.split(" ")[1]);

        setMessageForUser(callbackQuery, messageForUser);
        deleteReport(reportId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.DENY_REPORT_DATE_TIME;
    }

    private void deleteReport(Long reportId) {
        reportRepository.findById(reportId).ifPresent(report -> {
            chatMessageRepository.deleteChatMessageByReport(report);
            reportRepository.deleteById(reportId);
        });
    }

    private void setMessageForUser(CallbackQuery callbackQuery, SendMessage messageForUser) {
        messageForUser.setChatId(getChatIdFromUpdate(callbackQuery));
        messageForUser.setText("Отправка отчёта отменена");
    }

    private String getChatIdFromUpdate(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getChatId().toString();
    }
}
