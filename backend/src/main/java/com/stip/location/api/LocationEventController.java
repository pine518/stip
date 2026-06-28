package com.stip.location.api;

import com.stip.common.api.ApiResponse;
import com.stip.location.api.dto.BatchLocationEventRequest;
import com.stip.location.api.dto.BatchLocationEventResponse;
import com.stip.location.api.dto.LocationEventRequest;
import com.stip.location.api.dto.LocationEventResponse;
import com.stip.location.domain.LocationEventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/location-events")
public class LocationEventController {

    private final LocationEventService locationEventService;

    public LocationEventController(LocationEventService locationEventService) {
        this.locationEventService = locationEventService;
    }

    @PostMapping
    public ApiResponse<LocationEventResponse> create(@Valid @RequestBody LocationEventRequest request) {
        return ApiResponse.ok(locationEventService.accept(request));
    }

    @PostMapping("/batch")
    public ApiResponse<BatchLocationEventResponse> createBatch(@Valid @RequestBody BatchLocationEventRequest request) {
        int total = request.events().size();
        int accepted = 0;
        for (LocationEventRequest event : request.events()) {
            LocationEventResponse response = locationEventService.accept(event);
            if (response.accepted()) {
                accepted++;
            }
        }
        return ApiResponse.ok(new BatchLocationEventResponse(total, accepted, total - accepted));
    }
}
