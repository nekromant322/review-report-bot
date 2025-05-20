package com.nekromant.telegram.commands.notification;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.MessageContants;
import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.service.NotificationService;
import com.nekromant.telegram.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;

import static com.nekromant.telegram.contants.Command.ENABLE_NOTIFICATION;

@Component
public class EnablePayNotification extends MentoringReviewCommand {

    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private NotificationService notificationService;

    public EnablePayNotification() {
        super(ENABLE_NOTIFICATION.getAlias(), ENABLE_NOTIFICATION.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);
        ValidationUtils.validateArgumentsNumber(strings);

        try {
            Mentor mentor = mentorRepository.findMentorByMentorInfo_ChatId(user.getId());
            if (mentor == null) {
                message.setText("Ты не ментор");
                execute(absSender, message, user);
            } else {
                notificationService.enablePayNotification(strings);
                message.setText(MessageContants.SUCCESS_SET_NOTIFICATION);
                execute(absSender, message, user);
            }
        } catch (InvalidParameterException e) {
            message.setText("Укажи аргументы");
            execute(absSender, message, user);
        } catch (Exception e) {
            message.setText(MessageContants.FAILED_SET_NOTIFICATION);
            execute(absSender, message, user);
        }
    }
}
