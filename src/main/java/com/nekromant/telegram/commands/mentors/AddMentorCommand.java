package com.nekromant.telegram.commands.mentors;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.ADD_MENTOR;
import static com.nekromant.telegram.contants.MessageContants.MENTORS_LIST_CHANGED;
import static com.nekromant.telegram.contants.MessageContants.NOT_OWNER_ERROR;

@Component
public class AddMentorCommand extends MentoringReviewCommand {

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public AddMentorCommand() {
        super(ADD_MENTOR.getAlias(), ADD_MENTOR.getDescription());
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
            String newMentorUserName = arguments[0].replaceAll("@", "");
            String newMentorRoom = arguments[1];
            UserInfo mentorInfo = userInfoService.getUserInfo(newMentorUserName);
            mentorRepository.save(Mentor.builder().mentorInfo(mentorInfo).userName(newMentorUserName).isActive(true).roomUrl(newMentorRoom).build());
            userInfoService.promoteUserToMentor(newMentorUserName);

        } catch (Exception e) {
            message.setText(e.getMessage() + "\n" + "Пример: /add_mentor @Marandyuk_Anatolii https://meet.google.com/yfp-haps-mtz");
            execute(absSender, message, user);
        }

        message.setText(MENTORS_LIST_CHANGED);
        execute(absSender, message, user);
    }
}
