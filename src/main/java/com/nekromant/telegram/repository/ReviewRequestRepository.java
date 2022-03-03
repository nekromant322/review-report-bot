package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ReviewRequest;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface ReviewRequestRepository extends CrudRepository<ReviewRequest, Long> {
    ReviewRequest findReviewRequestByStudentChatId(String studentChatId);

    void deleteAllByBookedDateTimeIsBefore(LocalDateTime dateTime);

    List<ReviewRequest> findAllByBookedDateTimeBetween(LocalDateTime from, LocalDateTime to);

    List<ReviewRequest> findAll();
}
