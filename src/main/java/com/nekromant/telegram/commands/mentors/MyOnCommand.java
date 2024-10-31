package com.nekromant.telegram.commands.mentors;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.MY_OFF;
import static com.nekromant.telegram.contants.Command.MY_ON;

@Component
public class MyOnCommand extends MentoringReviewCommand {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public MyOnCommand() {
        super(MY_ON.getAlias(), MY_ON.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        message.disableNotification();
        try {
            Mentor mentor = mentorRepository.findMentorByMentorInfo_ChatId(user.getId());
            if (mentor == null) {
                message.setText("Ты не ментор");
                execute(absSender, message, user);
                return;
            }
            mentor.setIsActive(true);
            mentorRepository.save(mentor);
            message.setText("Теперь ты активный ментор, если сильно занят жми /" + MY_OFF.getAlias());
            execute(absSender, message, user);

        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

    }
}
