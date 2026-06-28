package com.stip.fence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFenceRequest(
        @NotBlank String fenceName,
        @NotBlank String fenceType,
        @NotBlank String ruleType,
        @NotBlank String geometryWkt,
        @NotNull Boolean enabled
) {
}

