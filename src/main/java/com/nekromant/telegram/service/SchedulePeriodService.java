package com.nekromant.telegram.service;

import com.nekromant.telegram.model.SchedulePeriod;
import com.nekromant.telegram.repository.SchedulePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class SchedulePeriodService {
    @Value("${schedulePeriod.start}")
    private Long defaultStart;
    @Value("${schedulePeriod.end}")
    private Long defaultEnd;

    @Autowired
    private SchedulePeriodRepository schedulePeriodRepository;

    private Map<String, Long> period = new HashMap<>();

    @PostConstruct
    public void setSchedulePeriod() {
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);
        Long start;
        Long end;

        if (schedulePeriod != null) {
            start = schedulePeriod.getStartTime();
            end = schedulePeriod.getEndTime();
        } else {
            start = defaultStart;
            end = defaultEnd;
        }

        period.put("start", start);
        period.put("end", end);
    }

    public Long getStart() {
        return period.get("start");
    }

    public Long getEnd() {
        return period.get("end");
    }

    public void setStart(Long start) {
        period.put("start", start);
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);

        if (schedulePeriod != null) {
            schedulePeriod.setStartTime(start);
            schedulePeriodRepository.save(schedulePeriod);
        } else {
            SchedulePeriod newSchedulePeriod = new SchedulePeriod();
            newSchedulePeriod.setId(1L);
            newSchedulePeriod.setStartTime(start);
            newSchedulePeriod.setEndTime(defaultEnd);
            schedulePeriodRepository.save(newSchedulePeriod);
        }
    }

    public void setEnd(Long end) {
        period.put("end", end);
        SchedulePeriod schedulePeriod = schedulePeriodRepository.findById(1L).orElse(null);

        if (schedulePeriod != null) {
            schedulePeriod.setEndTime(end);
            schedulePeriodRepository.save(schedulePeriod);
        } else {
            SchedulePeriod newSchedulePeriod = new SchedulePeriod();
            newSchedulePeriod.setId(1L);
            newSchedulePeriod.setStartTime(defaultStart);
            newSchedulePeriod.setEndTime(end);
            schedulePeriodRepository.save(newSchedulePeriod);
        }
    }
}
