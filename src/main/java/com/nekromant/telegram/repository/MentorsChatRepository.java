package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.SpecialChats;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MentorsChatRepository extends CrudRepository<SpecialChats, Long> {
    List<SpecialChats> findAll();
}
