package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Report;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends CrudRepository<Report, Long> {
    boolean existsReportByDateAndUserInfo(LocalDate date, UserInfo userInfo);

    List<Report> findByDateAndUserInfo(LocalDate date, UserInfo userInfo);

    List<Report> findAll(); //todo переписать на норм запрос для получения первого отчета

    List<Report> findAllByUserInfo_UserNameIgnoreCase(String studentUsername);

    List<Report> findAllByUserInfo(UserInfo userInfo);

    List<Report> findAllByUserInfo_ChatId(Long studentChatId);

    @Query("SELECT sum(r.hours) FROM Report r WHERE r.userInfo.chatId = ?1")
    Integer findTotalHoursByUserInfo_ChatId(Long studentChatId);

    @Query("SELECT count(r) FROM Report r WHERE (r.userInfo.chatId = ?1 AND r.hours > 0)")
    Integer findTotalStudyDaysByUserInfo_ChatId(Long studentChatId);

    List<Report> findAllByDateIs(LocalDate date);

    @Transactional
    void deleteByUserInfo_UserNameIgnoreCase(String studentUserName);
}
