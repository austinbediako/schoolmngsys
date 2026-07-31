package com.drakalabs.schoolmngsys.attendance.service;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceRecord;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordView(
        UUID id, UUID enrollmentId, LocalDate attendanceDate, AttendanceStatus status, String correctionReason) {

    public static AttendanceRecordView from(AttendanceRecord record) {
        return new AttendanceRecordView(
                record.getId(), record.getEnrollmentId(), record.getAttendanceDate(), record.getStatus(), record.getCorrectionReason());
    }
}
