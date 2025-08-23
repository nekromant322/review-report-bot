package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.NotificationReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface NotificationReportRepository extends CrudRepository<NotificationReport, Long> {

    @Query("SELECT f.enable FROM NotificationReport f")
    Boolean getFlag();

    NotificationReport getFirstByOrderByIdAsc();
}
