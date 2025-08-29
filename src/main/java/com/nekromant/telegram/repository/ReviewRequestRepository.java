package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ReviewRequest;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRequestRepository extends CrudRepository<ReviewRequest, Long> {
    void deleteAllByBookedDateTimeIsBefore(LocalDateTime dateTime);

    List<ReviewRequest> findAllByBookedDateTimeBetween(LocalDateTime from, LocalDateTime to);
    @Query(value = "SELECT * FROM review_request " +
            "WHERE booked_date_time IS NOT NULL " +
            "ORDER BY booked_date_time DESC " +
            "LIMIT 1", nativeQuery = true)
    ReviewRequest findFirstByOrderByBookedDateTimeDesc();
    List<ReviewRequest> findAll();

    boolean existsByBookedDateTimeAndMentorInfo(LocalDateTime dateTime, UserInfo userInfo);
}
