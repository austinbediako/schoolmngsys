package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Per (Student, Term): classPosition + remarks + publication; per-subject results are queried, not duplicated here. */
@Entity
@Table(name = "report_cards")
public class ReportCard extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false, updatable = false)
    private UUID enrollmentId;

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Column(name = "class_position")
    private Integer classPosition;

    @Column(name = "conduct_remark")
    private String conductRemark;

    @Column(name = "interest_remark")
    private String interestRemark;

    @Column(name = "head_remark")
    private String headRemark;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected ReportCard() {
    }

    public ReportCard(UUID enrollmentId, UUID termId) {
        this.enrollmentId = enrollmentId;
        this.termId = termId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getTermId() {
        return termId;
    }

    public Integer getClassPosition() {
        return classPosition;
    }

    public void assignClassPosition(int position) {
        this.classPosition = position;
    }

    public String getConductRemark() {
        return conductRemark;
    }

    public String getInterestRemark() {
        return interestRemark;
    }

    public String getHeadRemark() {
        return headRemark;
    }

    public void updateRemarks(String conductRemark, String interestRemark, String headRemark) {
        this.conductRemark = conductRemark;
        this.interestRemark = interestRemark;
        this.headRemark = headRemark;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void publish() {
        this.publishedAt = Instant.now();
    }
}
