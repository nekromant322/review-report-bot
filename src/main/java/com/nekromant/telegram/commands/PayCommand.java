package com.nekromant.telegram.commands;

import com.nekromant.telegram.utils.ValidationUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.PAY;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

@Component
public class PayCommand extends MentoringReviewCommand {
    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    public PayCommand() {
        super(PAY.getAlias(), PAY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        String studentChatId = chat.getId().toString();
        message.setChatId(studentChatId);

        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }

        try {
            ValidationUtils.validateArguments(strings);
            String phoneNumber = parsePhoneNumber(strings);
            String sum = parseSum(strings);
            message.setText("Выставлен счет на номер " + phoneNumber + " на сумму " + sum);
        } catch (Exception e) {
            message.setText("Пример: \n" +
                    "/pay 79775548911 5000.00");
            execute(absSender, message, user);
            return;
        }
        execute(absSender, message, user);
    }

    public String parsePhoneNumber(String[] strings){
        return strings[0];
    }

    public String parseSum(String[] strings){
        return strings[1];
    }

    @Data
    public static class ChequeDTO {
        @Value("${pay-info.login}")
        private String login;
        @Value("${pay-info.apikey}")
        private String apiKey;
        private String amount; // 10.0
        private String description;
        private String customerPhone; // 79057621525
        @Value("${pay-info.method}")
        private String method;
    }

}
