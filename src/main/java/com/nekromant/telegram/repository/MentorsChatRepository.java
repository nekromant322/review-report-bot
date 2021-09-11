package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.MentorsChat;
import com.nekromant.telegram.model.ReviewRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface MentorsChatRepository extends CrudRepository<MentorsChat, Long> {
    List<MentorsChat> findAll();
}
