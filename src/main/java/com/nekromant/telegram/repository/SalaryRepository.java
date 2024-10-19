package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Salary;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface SalaryRepository extends CrudRepository<Salary, String> {
    List<Salary> findAll();
}
