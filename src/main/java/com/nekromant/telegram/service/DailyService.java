package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Daily;
import com.nekromant.telegram.repository.DailyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class DailyService {

    @Autowired
    private DailyRepository dailyRepository;

    public void saveDaily(Daily daily) {
        LocalTime localTime = LocalTime.of(daily.getTime().getHour(), daily.getTime().getMinute());
        daily.setTime(localTime);
        dailyRepository.save(daily);
    }

    public List<Daily> getAllDaily() {
        return (List<Daily>) dailyRepository.findAll();
    }

    public Daily getDailyByTime(LocalTime time) {
        return dailyRepository.findByTime(time);
    }

    public void deleteDaily(Daily daily) {
        dailyRepository.deleteById(daily.getId());
    }
}
