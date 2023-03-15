package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.SchedulePeriod;
import org.springframework.data.repository.CrudRepository;

public interface SchedulePeriodRepository extends CrudRepository<SchedulePeriod, Long> {
}
