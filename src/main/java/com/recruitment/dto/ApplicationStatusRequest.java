package com.recruitment.dto;

import com.recruitment.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {}
