package com.drakalabs.schoolmngsys.attendance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/**
 * (Enrollment, date) — BR-AT-001 one record per student per school day. {@code enrollmentId} is a
 * plain UUID, not a JPA association (same cross-module pattern as {@code Enrollment} itself:
 * {@code enrollment}'s entities stay private to that module). {@code createdBy}/{@code createdAt}
 * from {@link BaseEntity} already mean "marked by"/"marked at" — no separate fields needed.
 */
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "attendance_date", nullable = false, updatable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "correction_reason")
    private String correctionReason;

    protected AttendanceRecord() {
    }

    public AttendanceRecord(UUID enrollmentId, LocalDate attendanceDate, AttendanceStatus status) {
        this.enrollmentId = enrollmentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public String getCorrectionReason() {
        return correctionReason;
    }

    public void correct(AttendanceStatus newStatus, String reason) {
        this.status = newStatus;
        this.correctionReason = reason;
    }
}
