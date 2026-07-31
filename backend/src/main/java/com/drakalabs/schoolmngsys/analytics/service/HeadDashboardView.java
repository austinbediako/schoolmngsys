package com.drakalabs.schoolmngsys.analytics.service;

import java.util.UUID;

public record HeadDashboardView(
        UUID activeAcademicYearId,
        String activeAcademicYearName,
        EnrollmentAnalyticsView enrollment,
        AttendanceAnalyticsView attendance,
        FinanceAnalyticsView finance,
        ResultsDistributionView resultsDistribution
) {
}
