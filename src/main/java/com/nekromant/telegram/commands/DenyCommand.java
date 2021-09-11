package com.nekromant.telegram.commands;


import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.security.InvalidParameterException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class DenyCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public DenyCommand() {
        super("deny", "Отменить ревью");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();

        try {
            validateArguments(arguments);
            Long reviewId = Long.parseLong(arguments[0]);

            ReviewRequest review = reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
            message.setChatId(review.getStudentChatId());
            message.setText("Никто не может провести ревью "+ review.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))  + " "
                    + review.getTimeSlots().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(":00, "))+":00" + "\n");

            reviewRequestRepository.deleteById(reviewId);
        } catch (Exception e) {
            message.setText(e.getMessage());
            execute(absSender, message, user);
        }

        execute(absSender, message, user);
    }


    private void validateArguments(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException();
        }
    }

}
