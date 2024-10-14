package com.nekromant.telegram.commands.mentors;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.DELETE_MENTOR;
import static com.nekromant.telegram.contants.MessageContants.MENTORS_LIST_CHANGED;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;


@Component
public class DeleteMentorCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public DeleteMentorCommand() {
        super(DELETE_MENTOR.getAlias(), DELETE_MENTOR.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        if (!user.getUserName().equals(ownerUserName)) {
            message.setText(NOT_OWNER_ERROR);
            execute(absSender, message, user);
            return;
        }

        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            String deleteMentorUserName = arguments[0].replaceAll("@", "");
            userInfoService.demoteMentorToUser(deleteMentorUserName);
        } catch (Exception e) {
            message.setText(e.getMessage() + "\n" + "Пример: /delete_mentor @Mentor_Telegram_UserName");
            execute(absSender, message, user);
        }

        message.setText(MENTORS_LIST_CHANGED);
        execute(absSender, message, user);
    }
}
