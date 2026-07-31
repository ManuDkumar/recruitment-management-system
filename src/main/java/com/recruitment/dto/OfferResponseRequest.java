package com.recruitment.dto;

import com.recruitment.enums.OfferStatus;
import jakarta.validation.constraints.NotNull;

public record OfferResponseRequest(
        @NotNull OfferStatus status
) {}
