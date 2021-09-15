package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.StepPassed;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;

public interface StepPassedRepository extends CrudRepository<StepPassed, Long> {
    List<StepPassed> findAll();

    List<StepPassed> findAllByStudentUserName(String studentUsername);

    List<StepPassed> findAllByDateIs(LocalDate date);

    @Transactional
    void deleteByStudentUserName(String studentUserName);

}
