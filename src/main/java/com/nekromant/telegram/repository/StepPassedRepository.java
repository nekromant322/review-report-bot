package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.StepPassed;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StepPassedRepository extends CrudRepository<StepPassed, Long> {
    List<StepPassed> findAll();
}
