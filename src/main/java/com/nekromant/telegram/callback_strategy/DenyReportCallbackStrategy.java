package com.nekromant.telegram.callback_strategy;

import com.nekromant.telegram.callback_strategy.delete_message_strategy.DeleteMessageStrategy;
import com.nekromant.telegram.callback_strategy.delete_message_strategy.MessagePart;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.contants.ChatType;
import com.nekromant.telegram.repository.ChatMessageRepository;
import com.nekromant.telegram.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
public class DenyReportCallbackStrategy implements CallbackStrategy {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public void executeCallbackQuery(Update update, Map<ChatType, SendMessage> messageMap, DeleteMessageStrategy deleteMessageStrategy) {
        SendMessage messageForUser = messageMap.get(ChatType.USER_CHAT);

        String callbackData = update.getCallbackQuery().getData();
        Long reportId = Long.parseLong(callbackData.split(" ")[1]);

        setMessageForUser(update, messageForUser);
        deleteReport(reportId);
        deleteMessageStrategy.setMessagePart(MessagePart.ENTIRE_MESSAGE);
    }

    @Override
    public CallBack getPrefix() {
        return CallBack.DENY_REPORT;
    }

    private void deleteReport(Long reportId) {
        reportRepository.findById(reportId).ifPresent(report -> {
            chatMessageRepository.deleteChatMessageByReport(report);
            reportRepository.deleteById(reportId);
        });
    }

    private void setMessageForUser(Update update, SendMessage messageForUser) {
        messageForUser.setChatId(getChatIdFromUpdate(update));
        messageForUser.setText("Отправка отчёта отменена");
    }

    private String getChatIdFromUpdate(Update update) {
        return update.getCallbackQuery().getMessage().getChatId().toString();
    }
}
