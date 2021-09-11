package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ReviewRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ReviewRequestRepository extends CrudRepository<ReviewRequest, Long> {
    ReviewRequest findReviewRequestByStudentChatId(String studentChatId);
}
