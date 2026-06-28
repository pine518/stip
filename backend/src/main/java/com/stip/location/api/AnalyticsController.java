package com.stip.location.api;

import com.stip.common.api.ApiResponse;
import com.stip.location.api.dto.AnalyticsOverviewResponse;
import com.stip.location.domain.LocationEventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final LocationEventService locationEventService;

    public AnalyticsController(LocationEventService locationEventService) {
        this.locationEventService = locationEventService;
    }

    @GetMapping("/overview")
    public ApiResponse<AnalyticsOverviewResponse> overview() {
        return ApiResponse.ok(locationEventService.overview());
    }
}

