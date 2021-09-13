package com.nekromant.telegram.commands.stat;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.nekromant.telegram.contants.Command.MY_STAT;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.USER_STAT_MESSAGE;

@Component
public class MyStatCommand extends MentoringReviewCommand {

    @Autowired
    private ReportService reportService;

    public MyStatCommand() {
        super(MY_STAT.getAlias(), MY_STAT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {

            UserStatistic userStats = reportService.getUserStats(user.getUserName());
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(String.format(USER_STAT_MESSAGE, userStats.getUserName(), userStats.getTotalDays(), userStats.getStudyDays(),
                    userStats.getTotalHours(), userStats.getAveragePerWeek()));
            execute(absSender, message, user);
        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR);
            execute(absSender, message, user);
        }
    }
}
