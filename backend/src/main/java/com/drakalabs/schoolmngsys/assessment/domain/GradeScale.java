package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/** BR-AA-001/005: one grade scale (bundling weighting + bands) per academic year, per A-03/A-04. */
@Entity
@Table(name = "grade_scales")
public class GradeScale extends BaseEntity {

    @Column(name = "academic_year_id", nullable = false, updatable = false)
    private UUID academicYearId;

    @Column(name = "sba_weight_percent", nullable = false)
    private BigDecimal sbaWeightPercent;

    @Column(name = "exam_weight_percent", nullable = false)
    private BigDecimal examWeightPercent;

    protected GradeScale() {
    }

    public GradeScale(UUID academicYearId, BigDecimal sbaWeightPercent, BigDecimal examWeightPercent) {
        this.academicYearId = academicYearId;
        this.sbaWeightPercent = sbaWeightPercent;
        this.examWeightPercent = examWeightPercent;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public BigDecimal getSbaWeightPercent() {
        return sbaWeightPercent;
    }

    public BigDecimal getExamWeightPercent() {
        return examWeightPercent;
    }
}
