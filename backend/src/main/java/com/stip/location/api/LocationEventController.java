package com.stip.location.api;

import com.stip.common.api.ApiResponse;
import com.stip.location.api.dto.BatchLocationEventRequest;
import com.stip.location.api.dto.BatchLocationEventResponse;
import com.stip.location.api.dto.LocationEventRequest;
import com.stip.location.api.dto.LocationEventResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/location-events")
public class LocationEventController {

    @PostMapping
    public ApiResponse<LocationEventResponse> create(@Valid @RequestBody LocationEventRequest request) {
        return ApiResponse.ok(new LocationEventResponse(null, true));
    }

    @PostMapping("/batch")
    public ApiResponse<BatchLocationEventResponse> createBatch(@Valid @RequestBody BatchLocationEventRequest request) {
        int total = request.events().size();
        return ApiResponse.ok(new BatchLocationEventResponse(total, total, 0));
    }
}

