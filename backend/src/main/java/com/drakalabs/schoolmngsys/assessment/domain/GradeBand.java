package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "grade_bands")
public class GradeBand extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_scale_id", nullable = false)
    private GradeScale gradeScale;

    @Column(name = "min_score", nullable = false)
    private BigDecimal minScore;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "description", nullable = false)
    private String description;

    protected GradeBand() {
    }

    public GradeBand(GradeScale gradeScale, BigDecimal minScore, BigDecimal maxScore, String grade, String description) {
        this.gradeScale = gradeScale;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.grade = grade;
        this.description = description;
    }

    public GradeScale getGradeScale() {
        return gradeScale;
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

    public boolean contains(BigDecimal score) {
        return score.compareTo(minScore) >= 0 && score.compareTo(maxScore) <= 0;
    }
}
