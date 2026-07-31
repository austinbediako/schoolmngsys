package com.drakalabs.schoolmngsys.analytics.api;

import com.drakalabs.schoolmngsys.analytics.service.AttendanceAnalyticsView;
import com.drakalabs.schoolmngsys.analytics.service.EnrollmentAnalyticsView;
import com.drakalabs.schoolmngsys.analytics.service.FinanceAnalyticsView;
import com.drakalabs.schoolmngsys.analytics.service.HeadDashboardView;
import com.drakalabs.schoolmngsys.analytics.service.ResultsDistributionView;
import java.util.UUID;

public record HeadDashboardResponse(
        UUID activeAcademicYearId,
        String activeAcademicYearName,
        EnrollmentAnalyticsView enrollment,
        AttendanceAnalyticsView attendance,
        FinanceAnalyticsView finance,
        ResultsDistributionView resultsDistribution
) {

    public static HeadDashboardResponse from(HeadDashboardView view) {
        return new HeadDashboardResponse(
                view.activeAcademicYearId(),
                view.activeAcademicYearName(),
                view.enrollment(),
                view.attendance(),
                view.finance(),
                view.resultsDistribution()
        );
    }
}
