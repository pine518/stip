package com.stip.alert.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AlertResponse(
        Long id,
        String alertType,
        String alertLevel,
        Long entityId,
        String entityCode,
        String entityName,
        Long fenceId,
        String message,
        BigDecimal longitude,
        BigDecimal latitude,
        OffsetDateTime eventTime,
        String status,
        Long handledBy,
        OffsetDateTime handledAt,
        OffsetDateTime createdAt
) {
}

