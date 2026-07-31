package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** BR-AS-006: a subject offered to a class for a year, with exactly one assigned teacher at a time. */
@Entity
@Table(name = "class_subject_offerings")
public class ClassSubjectOffering extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "teacher_staff_id")
    private UUID teacherStaffId;

    protected ClassSubjectOffering() {
    }

    public ClassSubjectOffering(SchoolClass schoolClass, Subject subject, AcademicYear academicYear) {
        this.schoolClass = schoolClass;
        this.subject = subject;
        this.academicYear = academicYear;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public Subject getSubject() {
        return subject;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public UUID getTeacherStaffId() {
        return teacherStaffId;
    }

    public void assignTeacher(UUID teacherStaffId) {
        this.teacherStaffId = teacherStaffId;
    }
}
