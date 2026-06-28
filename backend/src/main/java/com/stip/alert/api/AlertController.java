package com.stip.alert.api;

import com.stip.alert.api.dto.AlertResponse;
import com.stip.alert.api.dto.HandleAlertRequest;
import com.stip.alert.domain.AlertService;
import com.stip.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ApiResponse<List<AlertResponse>> list(@RequestParam(required = false) String status) {
        return ApiResponse.ok(alertService.findAll(status));
    }

    @PostMapping("/{id}/handle")
    public ApiResponse<AlertResponse> handle(@PathVariable Long id, @RequestBody(required = false) HandleAlertRequest request) {
        return ApiResponse.ok(alertService.handle(id, request == null ? new HandleAlertRequest(0L) : request));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<AlertResponse> close(@PathVariable Long id, @RequestBody(required = false) HandleAlertRequest request) {
        return ApiResponse.ok(alertService.close(id, request == null ? new HandleAlertRequest(0L) : request));
    }
}

