package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import com.drakalabs.schoolmngsys.attendance.service.AttendanceRecordView;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordResponse(UUID id, UUID enrollmentId, LocalDate attendanceDate, AttendanceStatus status, String correctionReason) {

    public static AttendanceRecordResponse from(AttendanceRecordView view) {
        return new AttendanceRecordResponse(view.id(), view.enrollmentId(), view.attendanceDate(), view.status(), view.correctionReason());
    }
}
