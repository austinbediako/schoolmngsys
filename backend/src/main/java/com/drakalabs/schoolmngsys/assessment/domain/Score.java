package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * BR-AA-002/007: bounded [0, component.maxScore]; exactly one of {@code rawScore} /
 * {@code exempted} / {@code naReason} must be set (schema-enforced, {@code ck_scores_resolution})
 * — an unresolved score blocks submission rather than defaulting to zero.
 */
@Entity
@Table(name = "scores")
public class Score extends BaseEntity {

    @Column(name = "assessment_component_id", nullable = false, updatable = false)
    private UUID assessmentComponentId;

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "raw_score")
    private BigDecimal rawScore;

    @Column(name = "exempted", nullable = false)
    private boolean exempted;

    @Column(name = "na_reason")
    private String naReason;

    protected Score() {
    }

    private Score(UUID assessmentComponentId, UUID enrollmentId, BigDecimal rawScore, boolean exempted, String naReason) {
        this.assessmentComponentId = assessmentComponentId;
        this.enrollmentId = enrollmentId;
        this.rawScore = rawScore;
        this.exempted = exempted;
        this.naReason = naReason;
    }

    public static Score scored(UUID assessmentComponentId, UUID enrollmentId, BigDecimal rawScore) {
        return new Score(assessmentComponentId, enrollmentId, rawScore, false, null);
    }

    public static Score exempted(UUID assessmentComponentId, UUID enrollmentId) {
        return new Score(assessmentComponentId, enrollmentId, null, true, null);
    }

    public static Score notApplicable(UUID assessmentComponentId, UUID enrollmentId, String reason) {
        return new Score(assessmentComponentId, enrollmentId, null, false, reason);
    }

    public UUID getAssessmentComponentId() {
        return assessmentComponentId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public BigDecimal getRawScore() {
        return rawScore;
    }

    public boolean isExempted() {
        return exempted;
    }

    public String getNaReason() {
        return naReason;
    }

    /** Contributes 0 to weighted totals — exempt/N-A students are excluded from the average, not zeroed. */
    public boolean countsTowardTotal() {
        return rawScore != null;
    }
}
