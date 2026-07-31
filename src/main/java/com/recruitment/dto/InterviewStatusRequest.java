package com.recruitment.dto;

import com.recruitment.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;

public record InterviewStatusRequest(
        @NotNull InterviewStatus status
) {}
