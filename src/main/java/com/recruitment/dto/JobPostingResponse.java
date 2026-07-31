package com.recruitment.dto;

import com.recruitment.enums.JobStatus;

import java.time.LocalDateTime;

public record JobPostingResponse(
        Long id,
        String title,
        String description,
        String location,
        String salaryRange,
        JobStatus status,
        Long companyId,
        String companyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
