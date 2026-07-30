package com.recruitment.repository;

import com.recruitment.enums.JobStatus;
import com.recruitment.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCompanyId(Long companyId);
    List<JobPosting> findByStatus(JobStatus status);
    List<JobPosting> findByCompanyIdAndStatus(Long companyId, JobStatus status);
}
