package com.nekromant.telegram.commands.mentors;

import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.SendMessageFactory;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.DELETE_MENTOR;
import static com.nekromant.telegram.contants.MessageContants.MENTORS_LIST_CHANGED;


@Component
public class DeleteMentorCommand extends OwnerCommand {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    @Autowired
    public DeleteMentorCommand() {
        super(DELETE_MENTOR.getAlias(), DELETE_MENTOR.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            String deleteMentorUserName = arguments[0].replaceAll("@", "");
            userInfoService.demoteMentorToDev(userInfoService.getUserInfo(deleteMentorUserName).getChatId());
        } catch (Exception e) {
            message.setText(e.getMessage() + "\n" + "Пример: /delete_mentor @Mentor_Telegram_UserName");
            execute(absSender, message, user);
        }

        message.setText(MENTORS_LIST_CHANGED);
        execute(absSender, message, user);
    }
}
