package com.nekromant.telegram.service.update_handler;

import com.nekromant.telegram.service.LocationService;
import com.nekromant.telegram.service.SendMessageService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.nekromant.telegram.contants.MessageContants.UNKNOWN_COMMAND;

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
        if(message.hasLocation()){
            locationService.setLocationUser(message);
            try {
                sendMessageService.sendMessage(getSendLocationMessage(message));
            }catch (TelegramApiException e){
                log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
            }
        }else{
            try {
                sendMessageService.sendMessage(getSendMessage(message));
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения {}", e.getMessage(), e);
            }
        }

    }

    private SendMessage getSendMessage(Message message) {
        return sendMessageFactory.create(String.valueOf(message.getChatId()), UNKNOWN_COMMAND);
    }

    private SendMessage getSendLocationMessage(Message message){
        return sendMessageFactory.create(String.valueOf(message.getChatId()), "Часовой пояс успешно установлен");
    }
}
