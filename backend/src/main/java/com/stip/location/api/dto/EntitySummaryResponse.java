package com.stip.location.api.dto;

public record EntitySummaryResponse(
        Long id,
        String entityCode,
        String entityName,
        String entityType,
        String status
) {
}

