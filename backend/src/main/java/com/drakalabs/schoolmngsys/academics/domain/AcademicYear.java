package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** BR-AS-001: exactly one ACTIVE year at a time (enforced by a partial unique index too). */
@Entity
@Table(name = "academic_years")
public class AcademicYear extends BaseEntity {

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcademicYearStatus status = AcademicYearStatus.PLANNED;

    protected AcademicYear() {
    }

    public AcademicYear(String label, LocalDate startDate, LocalDate endDate) {
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public AcademicYearStatus getStatus() {
        return status;
    }

    public void activate() {
        this.status = AcademicYearStatus.ACTIVE;
    }

    public void close() {
        this.status = AcademicYearStatus.CLOSED;
    }
}
