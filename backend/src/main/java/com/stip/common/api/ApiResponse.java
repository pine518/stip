package com.stip.common.api;

public record ApiResponse<T>(String code, String message, String traceId, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("0", "success", TraceIds.current(), data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, TraceIds.current(), null);
    }
}

