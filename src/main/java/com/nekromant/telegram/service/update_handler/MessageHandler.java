package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.service.LocationService;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.nekromant.telegram.contants.MessageContants.UNKNOWN_COMMAND;
import static com.nekromant.telegram.contants.MessageContants.TRUE_SET_LOCATION;
import static com.nekromant.telegram.contants.MessageContants.FALSE_SET_LOCATION;

@Slf4j
@Component
public class MessageHandler {

    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    @Autowired
    private LocationService locationService;


    public void handleMessage(Message message) {
        try {
            if (message.hasLocation()) {
                handleLocationMessage(message);
            } else {
                handleTextMessage(message);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }

    private void handleLocationMessage(Message message) throws TelegramApiException {
        boolean locationSet = locationService.setLocationUser(message);

        if (locationSet) {
            sendMessageService.sendMessage(getSendLocationMessage(message));
        } else {
            sendMessageService.sendMessage(getErrorSendLocationMessage(message));
        }
    }

    private void handleTextMessage(Message message) throws TelegramApiException {
        sendMessageService.sendMessage(getSendMessage(message));
    }

    private SendMessage getSendMessage(Message message) {
        return sendMessageFactory.create(String.valueOf(message.getChatId()), UNKNOWN_COMMAND);
    }

    private SendMessage getSendLocationMessage(Message message) {
        return sendMessageFactory.create(String.valueOf(message.getChatId()), TRUE_SET_LOCATION);
    }

    private SendMessage getErrorSendLocationMessage(Message message) {
        return sendMessageFactory.create(String.valueOf(message.getChatId()), FALSE_SET_LOCATION);
    }
}
