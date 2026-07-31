package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** A holiday or ad-hoc closure that overrides the default school-day computation (FR-ACAD-02). */
@Entity
@Table(name = "school_day_exceptions")
public class SchoolDayException extends BaseEntity {

    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", nullable = false)
    private SchoolDayExceptionType exceptionType;

    @Column(name = "reason")
    private String reason;

    protected SchoolDayException() {
    }

    public SchoolDayException(LocalDate exceptionDate, SchoolDayExceptionType exceptionType, String reason) {
        this.exceptionDate = exceptionDate;
        this.exceptionType = exceptionType;
        this.reason = reason;
    }

    public LocalDate getExceptionDate() {
        return exceptionDate;
    }

    public SchoolDayExceptionType getExceptionType() {
        return exceptionType;
    }

    public String getReason() {
        return reason;
    }
}
