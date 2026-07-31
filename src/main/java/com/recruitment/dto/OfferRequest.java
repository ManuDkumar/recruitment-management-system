package com.recruitment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferRequest(
        @NotNull @DecimalMin("0.0") BigDecimal salary,
        LocalDate joiningDate
) {}
