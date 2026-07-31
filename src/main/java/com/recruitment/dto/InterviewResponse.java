package com.recruitment.dto;

import com.recruitment.enums.InterviewStatus;
import com.recruitment.enums.InterviewType;

import java.time.LocalDateTime;

public record InterviewResponse(
        Long id,
        LocalDateTime scheduledAt,
        InterviewStatus status,
        InterviewType type,
        Long applicationId,
        Long interviewerId,
        String interviewerName
) {}
