package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * A computed snapshot for (Enrollment, Subject-offering, Term) — never hand-edited. BR-AA-006:
 * once PUBLISHED it is immutable; a correction archives this row and creates a new one with
 * {@code resultVersion + 1}, chained via {@code supersededById} on the old row.
 */
@Entity
@Table(name = "term_results")
public class TermResult extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "class_subject_offering_id", nullable = false, updatable = false)
    private UUID classSubjectOfferingId;

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Column(name = "sba_total", nullable = false)
    private BigDecimal sbaTotal;

    @Column(name = "exam_total", nullable = false)
    private BigDecimal examTotal;

    @Column(name = "weighted_total", nullable = false)
    private BigDecimal weightedTotal;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "subject_position")
    private Integer subjectPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResultStatus status = ResultStatus.DRAFT;

    @Column(name = "result_version", nullable = false)
    private int resultVersion = 1;

    @Column(name = "superseded_by_id")
    private UUID supersededById;

    @Column(name = "revision_reason")
    private String revisionReason;

    protected TermResult() {
    }

    public TermResult(
            UUID enrollmentId,
            UUID classSubjectOfferingId,
            UUID termId,
            BigDecimal sbaTotal,
            BigDecimal examTotal,
            BigDecimal weightedTotal,
            String grade) {
        this.enrollmentId = enrollmentId;
        this.classSubjectOfferingId = classSubjectOfferingId;
        this.termId = termId;
        this.sbaTotal = sbaTotal;
        this.examTotal = examTotal;
        this.weightedTotal = weightedTotal;
        this.grade = grade;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getClassSubjectOfferingId() {
        return classSubjectOfferingId;
    }

    public UUID getTermId() {
        return termId;
    }

    public BigDecimal getSbaTotal() {
        return sbaTotal;
    }

    public BigDecimal getExamTotal() {
        return examTotal;
    }

    public BigDecimal getWeightedTotal() {
        return weightedTotal;
    }

    public String getGrade() {
        return grade;
    }

    public Integer getSubjectPosition() {
        return subjectPosition;
    }

    public void assignSubjectPosition(int position) {
        this.subjectPosition = position;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public int getResultVersion() {
        return resultVersion;
    }

    public UUID getSupersededById() {
        return supersededById;
    }

    public String getRevisionReason() {
        return revisionReason;
    }

    public void submit() {
        this.status = ResultStatus.SUBMITTED;
    }

    public void approve() {
        this.status = ResultStatus.HOD_APPROVED;
    }

    public void publish() {
        this.status = ResultStatus.PUBLISHED;
    }

    /** BR-AA-006: this row becomes an immutable historical record once superseded. */
    public void supersede(UUID newResultId) {
        this.supersededById = newResultId;
        this.archive();
    }

    public void setRevisionInfo(int resultVersion, String revisionReason) {
        this.resultVersion = resultVersion;
        this.revisionReason = revisionReason;
    }
}
