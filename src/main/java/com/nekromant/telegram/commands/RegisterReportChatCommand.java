package com.nekromant.telegram.commands;

import com.nekromant.telegram.service.SpecialChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.REGISTER_REPORT;

@Component
public class RegisterReportChatCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    public RegisterReportChatCommand() {
        super(REGISTER_REPORT.getAlias(), REGISTER_REPORT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        if (!user.getUserName().equals(ownerUserName)) {
            message.setText("Ты не владелец бота");
            execute(absSender, message, user);
            return;
        }
        try {
            specialChatService.updateReportsChatId(chatId);
        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        message.setText("Этот чат теперь основной чат для отчетов, сюда будут приходить отчеты, которые пишут боту");
        execute(absSender, message, user);
    }
}

