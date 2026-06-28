package com.stip.location.api.dto;

public record AnalyticsOverviewResponse(
        int onlineEntities,
        int todayTrajectoryPoints,
        int enabledFences,
        int openAlerts
) {
}

