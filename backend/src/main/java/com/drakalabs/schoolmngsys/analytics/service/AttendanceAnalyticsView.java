package com.drakalabs.schoolmngsys.analytics.service;

import java.math.BigDecimal;

public record AttendanceAnalyticsView(
        long totalRecords,
        long presentCount,
        long absentCount,
        long lateCount,
        long excusedCount,
        BigDecimal attendanceRatePercentage
) {
}
