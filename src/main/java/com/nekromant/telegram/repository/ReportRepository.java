package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Report;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends CrudRepository<Report, Long> {
    Boolean existsReportByDateAndStudentUserName(LocalDate date, String studentUserName);

    List<Report> findAll(); //todo переписать на норм запрос для получения первого отчета

    List<Report> findAllByStudentUserNameIgnoreCase(String studentUsername);

    @Query("SELECT sum(r.hours) FROM Report r WHERE r.studentUserName = ?1")
    Integer findTotalHours(String studentUsername);

    @Query("SELECT count(r) FROM Report r WHERE (r.studentUserName = ?1 AND r.hours > 0)")
    Integer findTotalStudyDays(String studentUsername);

    List<Report> findAllByDateIs(LocalDate date);

    @Transactional
    void deleteByStudentUserName(String studentUserName);

}
