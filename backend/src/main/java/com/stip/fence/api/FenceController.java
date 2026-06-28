package com.stip.fence.api;

import com.stip.common.api.ApiResponse;
import com.stip.fence.api.dto.CreateFenceRequest;
import com.stip.fence.api.dto.FenceResponse;
import com.stip.fence.domain.FenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fences")
public class FenceController {

    private final FenceService fenceService;

    public FenceController(FenceService fenceService) {
        this.fenceService = fenceService;
    }

    @GetMapping
    public ApiResponse<List<FenceResponse>> list() {
        return ApiResponse.ok(fenceService.findAll());
    }

    @PostMapping
    public ApiResponse<FenceResponse> create(@Valid @RequestBody CreateFenceRequest request) {
        return ApiResponse.ok(fenceService.create(request));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<FenceResponse> setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request) {
        return ApiResponse.ok(fenceService.setEnabled(id, request.enabled()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fenceService.delete(id);
        return ApiResponse.ok(null);
    }

    public record EnabledRequest(boolean enabled) {
    }
}
