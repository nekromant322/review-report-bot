package com.nekromant.telegram.commands.mentors;


import com.nekromant.telegram.commands.OwnerCommand;
import com.nekromant.telegram.service.MentorService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.SendMessageFactory;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.ADD_MENTOR;
import static com.nekromant.telegram.contants.MessageContants.MENTORS_LIST_CHANGED;

@Component
public class AddMentorCommand extends OwnerCommand {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private MentorService mentorService;
    @Autowired
    private SendMessageFactory sendMessageFactory;

    @Autowired
    public AddMentorCommand() {
        super(ADD_MENTOR.getAlias(), ADD_MENTOR.getDescription());
    }

    @Override
    public void executeOwner(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        try {
            ValidationUtils.validateArgumentsNumber(arguments);
            String newMentorUserName = arguments[0].replaceAll("@", "");
            String newMentorRoom = arguments[1];

            mentorService.saveMentor(newMentorUserName, newMentorRoom);
            userInfoService.promoteUserToMentor(newMentorUserName);

            message.setText(MENTORS_LIST_CHANGED);
            execute(absSender, message, user);
        } catch (Exception e) {
            message.setText(e.getMessage() + "\n" + "Пример: /add_mentor @Marandyuk_Anatolii https://meet.google.com/yfp-haps-mtz");
            execute(absSender, message, user);
        }
    }
}
