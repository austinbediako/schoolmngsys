package com.drakalabs.schoolmngsys.attendance.service;

import java.util.UUID;

/** BR-AT-005 report-card input: "Present X out of Y school days", computed from records. */
public record AttendanceSummaryView(UUID enrollmentId, int totalSchoolDays, int presentEquivalent, int absentEquivalent) {
}
