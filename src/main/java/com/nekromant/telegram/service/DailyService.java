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
        dailyRepository.save(daily);
    }

    public List<Daily> getAllDailyByTime(LocalTime time) {
        return dailyRepository.findAllByTime(time);
    }

    public void deleteDailyByChatId(String message) {
        dailyRepository.deleteByChatId(message);
    }

}
