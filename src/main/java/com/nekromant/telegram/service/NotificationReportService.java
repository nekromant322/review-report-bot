package com.nekromant.telegram.service;

import com.nekromant.telegram.model.NotificationReport;
import com.nekromant.telegram.repository.NotificationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReportService {

    private final NotificationReportRepository notificationReportRepository;


    public void enableReportNotification() {
        NotificationReport notificationReport = notificationReportRepository.getFirstByOrderByIdAsc();
        notificationReport.setEnable(true);
        notificationReportRepository.save(notificationReport);
    }

    public void disableReportNotification() {
        NotificationReport notificationReport = notificationReportRepository.getFirstByOrderByIdAsc();
        notificationReport.setEnable(false);
        notificationReportRepository.save(notificationReport);
    }

    public boolean isEnabled() {
        return notificationReportRepository.getFlag();
    }
}
