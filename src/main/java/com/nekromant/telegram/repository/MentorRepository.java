package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Mentor;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface MentorRepository extends CrudRepository<Mentor, Long> {
    List<Mentor> findAll();

    List<Mentor> findAllByIsActiveIsTrue();

    Mentor findMentorByMentorInfo_ChatId(Long chatId);

    Mentor findMentorByMentorInfo(UserInfo mentorInfo);
}
