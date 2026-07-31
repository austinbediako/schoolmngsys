package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * BR-AA-001/002: (ClassSubjectOffering, Term), a category (SBA|EXAM), and a weight *within* that
 * category — a subject's several SBA components' weights sum to 100 among themselves, and the
 * category's overall share of the term result (30/70 by default) comes from the year's
 * {@link GradeScale}, not from here.
 */
@Entity
@Table(name = "assessment_components")
public class AssessmentComponent extends BaseEntity {

    @Column(name = "class_subject_offering_id", nullable = false, updatable = false)
    private UUID classSubjectOfferingId;

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, updatable = false)
    private AssessmentCategory category;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "weight_percent", nullable = false)
    private BigDecimal weightPercent;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    protected AssessmentComponent() {
    }

    public AssessmentComponent(
            UUID classSubjectOfferingId,
            UUID termId,
            String title,
            AssessmentCategory category,
            BigDecimal maxScore,
            BigDecimal weightPercent,
            LocalDate assessmentDate) {
        this.classSubjectOfferingId = classSubjectOfferingId;
        this.termId = termId;
        this.title = title;
        this.category = category;
        this.maxScore = maxScore;
        this.weightPercent = weightPercent;
        this.assessmentDate = assessmentDate;
    }

    public UUID getClassSubjectOfferingId() {
        return classSubjectOfferingId;
    }

    public UUID getTermId() {
        return termId;
    }

    public String getTitle() {
        return title;
    }

    public AssessmentCategory getCategory() {
        return category;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public BigDecimal getWeightPercent() {
        return weightPercent;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }
}
