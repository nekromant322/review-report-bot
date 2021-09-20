package com.nekromant.telegram.commands.stat;

import com.nekromant.telegram.commands.MentoringReviewCommand;
import com.nekromant.telegram.model.UserStatistic;
import com.nekromant.telegram.service.ActualStatPhotoHolderService;
import com.nekromant.telegram.service.ReportService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekromant.telegram.contants.Command.ALL_STAT;
import static com.nekromant.telegram.contants.MessageContants.ERROR;
import static com.nekromant.telegram.contants.MessageContants.USER_STAT_MESSAGE;

@Component
public class AllStatCommand extends MentoringReviewCommand {

    @Autowired
    private ReportService reportService;

    @Value("${server.host}")
    private String appHost;

    @Autowired
    private ActualStatPhotoHolderService photoHolderService;

    public AllStatCommand() {
        super(ALL_STAT.getAlias(), ALL_STAT.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {

            String allStatsMessage = "\uD83D\uDE80 " + reportService.getAllUsersStats().stream()
                    .sorted(Comparator.comparing(UserStatistic::getAveragePerWeek).reversed())
                    .map(stat -> String.format(USER_STAT_MESSAGE, stat.getUserName(), stat.getTotalDays(), stat.getStudyDays(),
                            stat.getTotalHours(), stat.getAveragePerWeek()))
                    .collect(Collectors.joining("\n\n"));

            allStatsMessage += "\n\n" + appHost + "/charts.html";
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(allStatsMessage);
            message.disableNotification();
            execute(absSender, message, user);

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chat.getId().toString());

            InputFile inputFile = new InputFile();
            String encodedString = photoHolderService.getEncodedPerDayGraph();
            File fileForPhoto = new File("allStatPhoto:" + LocalDateTime.now().toString());
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            FileUtils.writeByteArrayToFile(fileForPhoto, decodedBytes);

            inputFile.setMedia(fileForPhoto);
            sendPhoto.setPhoto(inputFile);
            absSender.execute(sendPhoto);

        } catch (Exception e) {
            e.printStackTrace();
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText(ERROR);
            execute(absSender, message, user);
        }
    }
}
