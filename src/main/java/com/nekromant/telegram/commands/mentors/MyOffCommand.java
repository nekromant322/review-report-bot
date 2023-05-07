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

import static com.nekromant.telegram.contents.Command.MY_OFF;
import static com.nekromant.telegram.contents.Command.MY_ON;

@Component
public class MyOffCommand extends MentoringReviewCommand {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public MyOffCommand() {
        super(MY_OFF.getAlias(), MY_OFF.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        message.disableNotification();

        try {
            Mentor mentor = mentorRepository.findMentorByUserName(user.getUserName());
            if (mentor == null) {
                message.setText("Ты не ментор");
                execute(absSender, message, user);
                return;
            }
            mentor.setIsActive(false);
            mentorRepository.save(mentor);
            message.setText("Теперь ты пассивный ментор, чтобы вернуться жми /" + MY_ON.getAlias());
            execute(absSender, message, user);

        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

    }
}
