package com.nekromant.telegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final UserInfoService userInfoService;


    public boolean setLocationUser(Message message) {
        log.info("Установка локации пользователя: " + message.getChat().getUserName());

        Location location = message.getLocation();
        long chatId = message.getChatId();

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        String timezone;
        try {
            timezone = TimezoneService.getTimezone(lat, lon);
        } catch (Exception e) {
            log.error("Ошибка при получении временной зоны для пользователя: " + chatId, e);
            return false;
        }

        return userInfoService.updateTimezone(chatId, timezone);
    }
}
