package com.stip.location.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchLocationEventRequest(@NotEmpty List<@Valid LocationEventRequest> events) {
}

