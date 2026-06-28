package com.stip.fence.api.dto;

import java.time.OffsetDateTime;

public record FenceResponse(
        Long id,
        String fenceName,
        String fenceType,
        String ruleType,
        String geometryGeoJson,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

