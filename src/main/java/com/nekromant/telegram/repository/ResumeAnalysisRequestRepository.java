package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.ResumeAnalysisRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeAnalysisRequestRepository extends CrudRepository<ResumeAnalysisRequest, Long> {
}
