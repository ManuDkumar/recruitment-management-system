package com.recruitment.dto;

import com.recruitment.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        Long jobId,
        String jobTitle,
        String companyName,
        Long candidateId,
        String candidateName
) {}
