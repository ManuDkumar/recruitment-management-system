package com.recruitment.dto;

import com.recruitment.enums.OfferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OfferResponse(
        Long id,
        BigDecimal salary,
        LocalDate joiningDate,
        OfferStatus status,
        LocalDateTime createdAt,
        Long applicationId,
        String jobTitle,
        String candidateName
) {}
