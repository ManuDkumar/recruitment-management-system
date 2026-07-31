package com.recruitment.dto;

import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull Long jobId
) {}
