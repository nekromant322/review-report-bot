package com.nekromant.telegram.commands.review;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REVIEW_TODAY;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;
import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class ReviewTodayCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired
    public ReviewTodayCommand() {
        super(REVIEW_TODAY.getAlias(), REVIEW_TODAY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        List<ReviewRequest> reviewsToday = reviewRequestRepository
                .findAllByBookedDateTimeBetween(
                        LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay(),
                        LocalDate.now(ZoneId.of("Europe/Moscow")).plus(1, DAYS).atStartOfDay()
                );

        String messageWithReviewsToday = "Расписание ревью на сегодня\n\n" +
                reviewsToday.stream()
                        .sorted(Comparator.comparing(ReviewRequest::getBookedDateTime))
                        .map(review ->
                                "@" + review.getStudentUserName() + "\n" +
                                        review.getBookedDateTime().format(defaultDateTimeFormatter()) + "\n" +
                                        review.getTitle() + "\n" +
                                        "@" + review.getMentorUserName() + "\n")
                        .collect(Collectors.joining("\n"));
        message.setText(messageWithReviewsToday);

        execute(absSender, message, user);
    }
}
