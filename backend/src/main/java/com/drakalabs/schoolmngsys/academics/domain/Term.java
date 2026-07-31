package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** One of exactly three divisions of an {@link AcademicYear} (BR-AS-001). */
@Entity
@Table(name = "terms")
public class Term extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "term_number", nullable = false)
    private int termNumber;

    @Column(name = "official_start_date", nullable = false)
    private LocalDate officialStartDate;

    @Column(name = "official_end_date", nullable = false)
    private LocalDate officialEndDate;

    @Column(name = "expected_school_days", nullable = false)
    private int expectedSchoolDays;

    protected Term() {
    }

    public Term(
            AcademicYear academicYear,
            int termNumber,
            LocalDate officialStartDate,
            LocalDate officialEndDate,
            int expectedSchoolDays) {
        this.academicYear = academicYear;
        this.termNumber = termNumber;
        this.officialStartDate = officialStartDate;
        this.officialEndDate = officialEndDate;
        this.expectedSchoolDays = expectedSchoolDays;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public int getTermNumber() {
        return termNumber;
    }

    public LocalDate getOfficialStartDate() {
        return officialStartDate;
    }

    public LocalDate getOfficialEndDate() {
        return officialEndDate;
    }

    public int getExpectedSchoolDays() {
        return expectedSchoolDays;
    }
}
