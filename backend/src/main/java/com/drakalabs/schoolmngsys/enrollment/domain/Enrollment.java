package com.drakalabs.schoolmngsys.enrollment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/**
 * (Student, Class, AcademicYear) — the historical spine of the system (docs/02 §5, ADR-006):
 * "which class was this student in, in 2027/28?" is always answerable, and rows are never deleted
 * (BR-EN-003), only status-transitioned. {@code studentId}/{@code classId}/{@code academicYearId}
 * are plain UUIDs, not JPA associations — {@code people} and {@code academics} entities stay
 * private to their own modules (docs/08 §2); enrollment composes with them via their query
 * services, the same pattern used for every other cross-module reference so far.
 */
@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "academic_year_id", nullable = false, updatable = false)
    private UUID academicYearId;

    @Column(name = "roll_number")
    private Integer rollNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "exit_reason")
    private String exitReason;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    protected Enrollment() {
    }

    public Enrollment(UUID studentId, UUID classId, UUID academicYearId, Integer rollNumber) {
        this.studentId = studentId;
        this.classId = classId;
        this.academicYearId = academicYearId;
        this.rollNumber = rollNumber;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getClassId() {
        return classId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public Integer getRollNumber() {
        return rollNumber;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public String getExitReason() {
        return exitReason;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    /** BR-EN-003: status transition only, the row itself is never deleted. */
    public void exit(EnrollmentStatus exitStatus, String reason, LocalDate exitDate) {
        this.status = exitStatus;
        this.exitReason = reason;
        this.exitDate = exitDate;
    }
}
