package com.edgepulse.dto;

public record DashboardSummary(
        long totalDevices,
        long onlineDevices,
        long activeAlarms,
        long todayAlarms
) {
}
