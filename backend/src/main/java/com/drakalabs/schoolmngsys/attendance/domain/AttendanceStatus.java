package com.drakalabs.schoolmngsys.attendance.domain;

/** BR-AT-003. LATE counts as present, EXCUSED counts as absent-with-reason in aggregates (A-07). */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}
