package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.service.DailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class DailyScheduler {

    @Autowired
    private DailyService dailyService;

    @Autowired
    MentoringReviewBot mentoringReviewBot;


    @Scheduled(cron = "0 1 * * * *")
    public void processEveryMinute() {
        notifyDaily();
    }

    public void notifyDaily() {
        System.out.println("Отправка уведомлений");
        // LocalTime nowInMoscow1 = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalTime();
        LocalTime nowInMoscow = LocalTime.now(ZoneId.of("Europe/Moscow"));
        LocalTime localTime = LocalTime.of(nowInMoscow.getHour(), nowInMoscow.getMinute());
        Daily daily = dailyService.getDailyByTime(localTime);

        if (daily != null) {
            String dailyMessage = daily.getMessage();
            mentoringReviewBot.sendMessage(daily.getChatId(), dailyMessage);
        }

    }

}
