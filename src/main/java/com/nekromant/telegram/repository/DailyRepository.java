package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Daily;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;

public interface DailyRepository extends CrudRepository<Daily, Long> {
    Daily findByTime(LocalTime localTime);

}
