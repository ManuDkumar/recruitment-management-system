package com.recruitment.repository;

import com.recruitment.enums.ApplicationStatus;
import com.recruitment.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateId(Long candidateId);
    List<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);
    List<Application> findByJobPostingId(Long jobPostingId);
    List<Application> findByStatus(ApplicationStatus status);
    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);
    long countByJobPostingId(Long jobPostingId);
    long countByStatus(ApplicationStatus status);
}
