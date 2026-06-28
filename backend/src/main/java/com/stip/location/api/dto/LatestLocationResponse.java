package com.stip.location.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LatestLocationResponse(
        Long entityId,
        String entityCode,
        String entityName,
        String entityType,
        BigDecimal longitude,
        BigDecimal latitude,
        BigDecimal speed,
        BigDecimal direction,
        OffsetDateTime eventTime
) {
}

