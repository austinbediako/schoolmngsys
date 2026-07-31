package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.service.AttendanceSummaryView;
import java.util.UUID;

public record AttendanceSummaryResponse(UUID enrollmentId, int totalSchoolDays, int presentEquivalent, int absentEquivalent) {

    public static AttendanceSummaryResponse from(AttendanceSummaryView view) {
        return new AttendanceSummaryResponse(view.enrollmentId(), view.totalSchoolDays(), view.presentEquivalent(), view.absentEquivalent());
    }
}
