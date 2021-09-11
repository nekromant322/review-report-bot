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

@Component
public class ApproveCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    public ApproveCommand() {
        super("approve", "Апрувнуть ревью");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();

        try {
            validateArguments(arguments);
            Long reviewId = Long.parseLong(arguments[0]);
            Integer timeSlot = Integer.parseInt(arguments[1]);
            ReviewRequest review = reviewRequestRepository.findById(reviewId).orElseThrow(InvalidParameterException::new);
            review.setBookedTimeSlot(timeSlot);
            review.setMentorUserName(user.getUserName());
            reviewRequestRepository.save(review);
            message.setChatId(review.getStudentChatId());
            message.setText("Ревью c " + "@"+review.getMentorUserName() + " назначено на \n"+ review.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))  + " " + review.getBookedTimeSlot()+":00" + "\n" + review.getTitle());

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
