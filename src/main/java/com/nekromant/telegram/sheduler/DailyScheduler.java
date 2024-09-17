package com.nekromant.telegram.sheduler;

import com.nekromant.telegram.MentoringReviewBot;
import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.service.DailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class DailyScheduler {
    @Autowired
    private DailyService dailyService;
    @Autowired
    private MentoringReviewBot mentoringReviewBot;

    @Scheduled(cron = "59 * * * * *")
    public void processEveryMinute() {
        notifyDaily();
    }

    public void notifyDaily() {
        log.info("Отправка уведомлений");
        LocalTime nowInMoscow = LocalTime.now(ZoneId.of("Europe/Moscow"));
        LocalTime localTime = LocalTime.of(nowInMoscow.getHour(), nowInMoscow.getMinute());
        List<Daily> dailyList = dailyService.getAllDailyByTime(localTime);

        for (Daily daily : dailyList) {
            mentoringReviewBot.sendMessage(daily.getChatId(), daily.getMessage());
        }
    }

}
