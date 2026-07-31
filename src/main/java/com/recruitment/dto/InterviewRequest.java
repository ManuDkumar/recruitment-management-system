package com.recruitment.dto;

import com.recruitment.enums.InterviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InterviewRequest(
        @NotNull LocalDateTime scheduledAt,
        @NotNull InterviewType type,
        @NotBlank String interviewerEmail
) {}
