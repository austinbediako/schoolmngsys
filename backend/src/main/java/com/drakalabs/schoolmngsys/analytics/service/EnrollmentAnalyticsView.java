package com.drakalabs.schoolmngsys.analytics.service;

import java.util.Map;

public record EnrollmentAnalyticsView(
        long totalActiveEnrollments,
        long maleCount,
        long femaleCount,
        long unknownGenderCount,
        Map<String, Long> countByClassLevel
) {
}
