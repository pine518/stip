package com.stip.location.api;

import com.stip.common.api.ApiResponse;
import com.stip.location.api.dto.EntitySummaryResponse;
import com.stip.location.api.dto.LatestLocationResponse;
import com.stip.location.api.dto.TrajectoryPointResponse;
import com.stip.location.domain.LocationEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/entities")
public class EntityController {

    private final LocationEventService locationEventService;

    public EntityController(LocationEventService locationEventService) {
        this.locationEventService = locationEventService;
    }

    @GetMapping
    public ApiResponse<List<EntitySummaryResponse>> list() {
        return ApiResponse.ok(locationEventService.findEntities());
    }

    @GetMapping("/latest-locations")
    public ApiResponse<List<LatestLocationResponse>> latestLocations() {
        return ApiResponse.ok(locationEventService.findLatestLocations());
    }

    @GetMapping("/{entityId}/trajectory")
    public ApiResponse<List<TrajectoryPointResponse>> trajectory(
            @PathVariable Long entityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime
    ) {
        return ApiResponse.ok(locationEventService.findTrajectory(entityId, startTime, endTime));
    }
}

