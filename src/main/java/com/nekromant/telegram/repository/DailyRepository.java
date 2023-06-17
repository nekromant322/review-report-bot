package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Daily;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

public interface DailyRepository extends CrudRepository<Daily, Long> {
    List<Daily> findAllByTime(LocalTime localTime);

    @Transactional
    void deleteByChatId(String message);

}
