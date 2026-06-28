package com.stip.location.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TrajectoryPointResponse(
        Long id,
        BigDecimal longitude,
        BigDecimal latitude,
        BigDecimal speed,
        BigDecimal direction,
        OffsetDateTime eventTime
) {
}

