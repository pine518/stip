package com.stip.location.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LocationEventRequest(
        @NotBlank String eventId,
        @NotBlank String entityCode,
        @NotBlank String entityType,
        @NotBlank String sourceType,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        BigDecimal speed,
        BigDecimal direction,
        BigDecimal accuracy,
        @NotNull OffsetDateTime eventTime
) {
}

