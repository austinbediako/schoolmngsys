package com.drakalabs.schoolmngsys.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/** The A-04 template (docs/14 §4) copied into a real {@link GradeScale}'s bands — read-only, seeded. */
@Entity
@Table(name = "default_grade_bands")
public class DefaultGradeBand {

    @Id
    private UUID id;

    @Column(name = "min_score", nullable = false)
    private BigDecimal minScore;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "description", nullable = false)
    private String description;

    protected DefaultGradeBand() {
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public String getGrade() {
        return grade;
    }

    public String getDescription() {
        return description;
    }
}
