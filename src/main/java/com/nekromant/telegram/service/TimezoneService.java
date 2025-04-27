package com.nekromant.telegram.service;

import com.nekromant.telegram.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

@Slf4j
@Service
public class TimezoneService {

    @Value("${timezone.api-key}")
    private static String API_KEY;

    private final TimeZone timeZoneProject = TimeZone.getTimeZone("Europe/Moscow");

    public static String getTimezone(double latitude, double longitude) {
        String lat = Double.toString(latitude);
        String lng = Double.toString(longitude);

        String url = String.format("http://api.timezonedb.com/v2.1/get-time-zone?key=%s&format=json&by=position&lat=%s&lng=%s",
                API_KEY, lat, lng);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Error occurred while fetching timezone", e);
            return null;
        }

        String responseBody = response.body();

        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (response.statusCode() == 200) {
                return jsonResponse.getString("zoneName");
            } else {
                log.warn("Failed to get timezone. Status: " + jsonResponse.getString("status"));
                return null;
            }
        } catch (Exception e) {
            log.error("Error parsing the response", e);
            return null;
        }
    }

    public Set<Integer> parseTimeSlotsToMoscow(UserInfo userInfo, Set<Integer> timeSlotsUser) {
        Set<Integer> moscowTimeSlots = new TreeSet<>();
        TimeZone timeZoneUser = TimeZone.getTimeZone(userInfo.getTimezone());
        if (userInfo.getTimezone() == null || userInfo.getTimezone().equals("Europe/Moscow")) {
            return timeSlotsUser;
        } else {
            int timeSlot;
            int diffTimeZone = getHoursDifference(timeZoneProject, timeZoneUser);
            for (int slot : timeSlotsUser) {
                timeSlot = slot + diffTimeZone;
                moscowTimeSlots.add(parseCurrentSlot(timeSlot));
            }
            return moscowTimeSlots;
        }
    }

    private int parseCurrentSlot(int slot) {
        if (slot > 0) {
            return slot;
        }
        if (slot == 0) {
            return 0;
        }

        return -24 - slot;
    }

    public static int getHoursDifference(TimeZone tz1, TimeZone tz2) {
        ZoneId zone1 = tz1.toZoneId();
        ZoneId zone2 = tz2.toZoneId();

        ZonedDateTime now = ZonedDateTime.now();

        ZoneOffset offset1 = zone1.getRules().getOffset(now.toInstant());
        ZoneOffset offset2 = zone2.getRules().getOffset(now.toInstant());

        return offset1.getTotalSeconds() / 3600 - offset2.getTotalSeconds() / 3600;
    }

    public LocalDateTime convertToUserZone(LocalDateTime inputDateTime, UserInfo user) {
        if (user.getTimezone() == null) {
            return inputDateTime;
        }
        ZoneId userZone = ZoneId.of(user.getTimezone());

        ZoneId projectZonedDateTime = timeZoneProject.toZoneId();

        ZonedDateTime globalZonedDateTime = inputDateTime.atZone(projectZonedDateTime);

        ZonedDateTime userZonedDateTime = globalZonedDateTime.withZoneSameInstant(userZone);
        return userZonedDateTime.toLocalDateTime();
    }


}
