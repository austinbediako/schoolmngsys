package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * BR-AS-005/A-01: one Class Teacher per Class per Year, and a teacher class-teaches at most one
 * Class per year (both enforced by service-level checks + partial unique indexes as the last net).
 * {@code teacherStaffId} is an opaque reference — {@code people}/Staff (WP-3) doesn't exist yet.
 */
@Entity
@Table(name = "class_teacher_assignments")
public class ClassTeacherAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "teacher_staff_id", nullable = false)
    private UUID teacherStaffId;

    protected ClassTeacherAssignment() {
    }

    public ClassTeacherAssignment(SchoolClass schoolClass, AcademicYear academicYear, UUID teacherStaffId) {
        this.schoolClass = schoolClass;
        this.academicYear = academicYear;
        this.teacherStaffId = teacherStaffId;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public UUID getTeacherStaffId() {
        return teacherStaffId;
    }
}
