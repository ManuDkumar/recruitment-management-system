package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobPostingRequest(
        @NotBlank String title,
        String description,
        String location,
        String salaryRange,
        @NotNull Long companyId
) {}
