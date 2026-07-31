package com.recruitment.dto;

import com.recruitment.enums.JobStatus;
import jakarta.validation.constraints.NotNull;

public record JobStatusRequest(
        @NotNull JobStatus status
) {}
