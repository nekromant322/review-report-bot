package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRequestRepository extends CrudRepository<ReviewRequest, Long> {
    void deleteAllByBookedDateTimeIsBefore(LocalDateTime dateTime);

    List<ReviewRequest> findAllByBookedDateTimeBetween(LocalDateTime from, LocalDateTime to);

    List<ReviewRequest> findAll();

    boolean existsByBookedDateTimeAndMentorInfo(LocalDateTime dateTime, UserInfo userInfo);
}
