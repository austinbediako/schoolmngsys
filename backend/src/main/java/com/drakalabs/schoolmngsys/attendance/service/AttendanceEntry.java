package com.drakalabs.schoolmngsys.attendance.service;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import java.util.UUID;

public record AttendanceEntry(UUID enrollmentId, AttendanceStatus status) {
}
