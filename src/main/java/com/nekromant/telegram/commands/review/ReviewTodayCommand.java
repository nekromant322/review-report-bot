package com.nekromant.telegram.commands.review;


import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.contants.CallBack;
import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.repository.MentorRepository;
import com.nekromant.telegram.repository.ReviewRequestRepository;
import com.nekromant.telegram.service.SpecialChatService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.REVIEW_TODAY;
import static com.nekromant.telegram.utils.FormatterUtils.defaultDateTimeFormatter;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Component
public class ReviewTodayCommand extends MentoringReviewCommand {

    @Autowired
    private ReviewRequestRepository reviewRequestRepository;

    @Autowired

    private MentorRepository mentorRepository;

    @Autowired
    private SpecialChatService specialChatService;

    @Autowired
    public ReviewTodayCommand() {
        super(REVIEW_TODAY.getAlias(), REVIEW_TODAY.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        String chatId = chat.getId().toString();
        message.setChatId(chatId);

        if (chat.getId().equals(Long.valueOf(specialChatService.getMentorsChatId()))) {
            writeMentorsCancelButtons(absSender, reviewRequestRepository
                    .findAllByBookedDateTimeBetween(
                            LocalDate.now(ZoneId.of("Europe/Moscow")).atStartOfDay(),
                            LocalDate.now(ZoneId.of("Europe/Moscow")).plus(3, DAYS).atStartOfDay()
                    ));
        } else {
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
                                            "@" + review.getMentorUserName() + "\n" +
                                            mentorRepository.findMentorByUserName(review.getMentorUserName()).getRoomUrl() + "\n")
                            .collect(Collectors.joining("\n"));
            message.setText(messageWithReviewsToday);
            message.disableWebPagePreview();
        }

        execute(absSender, message, user);
    }

    @SneakyThrows
    private void writeMentorsCancelButtons(AbsSender absSender, List<ReviewRequest> reviewRequestList) {


        reviewRequestList.forEach(x -> {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton
                    .setText("Отменить");
            inlineKeyboardButton.setCallbackData(CallBack.DENY.getAlias() + " " + x.getId());

            keyboardButtonRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonRow);
            inlineKeyboardMarkup.setKeyboard(rowList);
            SendMessage message = new SendMessage();
            message.setChatId(specialChatService.getMentorsChatId());


            message.setText("Ревью @" + x.getStudentUserName() + " c @" + x.getMentorUserName() + " " +
                    x.getBookedDateTime().format(defaultDateTimeFormatter()));
            message.setReplyMarkup(inlineKeyboardMarkup);


            try {
                Message executedMessage = absSender.execute(message);
                x.setPollMessageId(executedMessage.getMessageId());
                reviewRequestRepository.save(x);
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        });


    }
}
