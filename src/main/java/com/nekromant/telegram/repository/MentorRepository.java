package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Mentor;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface MentorRepository extends CrudRepository<Mentor, String> {
    List<Mentor> findAll();

    List<Mentor> findAllByIsActiveIsTrue();

    Mentor findMentorByUserName(String userName);
}
