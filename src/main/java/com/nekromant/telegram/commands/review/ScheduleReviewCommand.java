package com.nekromant.telegram.commands.review;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.service.ReviewScheduleService;
import com.nekromant.telegram.service.UserInfoService;
import com.nekromant.telegram.utils.SendMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.nekromant.telegram.contants.Command.SCHEDULE_REVIEW;

@Slf4j
@Component
public class ScheduleReviewCommand extends MentoringReviewCommand {
    @Autowired
    private SendMessageFactory sendMessageFactory;
    @Autowired
    private ReviewScheduleService reviewScheduleService;
    @Autowired
    private UserInfoService userInfoService;


    public ScheduleReviewCommand() {
        super(SCHEDULE_REVIEW.getAlias(), SCHEDULE_REVIEW.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = sendMessageFactory.create(chat);
        UserInfo userInfo = userInfoService.getUserInfo(user.getId());
        if (arguments.length == 0) {
            message.setText(reviewScheduleService.getSchedule(userInfo));
        } else if (arguments.length == 2) {
            try {
                LocalDateTime[] dates = argumentsParser(arguments);
                message.setText(reviewScheduleService.getSchedule(userInfo, dates[0], dates[1]));
            } catch (DateTimeParseException e) {
                message.setText("Неверный формат даты, попробуй:\n/schedule <dd.MM.yyyy> <dd.MM.yyyy>");
            }
        } else {
            message.setText("Неверное количество аргументов, допустимые варианты:\n/schedule\n/schedule <dd.MM.yyyy> <dd.MM.yyyy>");
        }
        message.disableWebPagePreview();
        execute(absSender, message, user);
    }


    private LocalDateTime[] argumentsParser(String[] arguments) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime t = LocalDate.parse(arguments[0], formatter).atStartOfDay();
        LocalDateTime t2 = LocalDate.parse(arguments[1], formatter).atStartOfDay();
        return new LocalDateTime[]{t, t2};
    }
}
