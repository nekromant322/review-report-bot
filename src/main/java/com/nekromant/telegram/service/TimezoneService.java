package com.nekromant.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Slf4j
@Service
public class TimezoneService {
    private static final String API_KEY = "B4JD7I3D29H0";

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
}
