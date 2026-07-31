package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "promotion_decisions")
public class PromotionDecision extends BaseEntity {

    @Column(name = "promotion_run_id", nullable = false)
    private UUID promotionRunId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "source_class_id", nullable = false)
    private UUID sourceClassId;

    @Column(name = "source_class_level_id", nullable = false)
    private UUID sourceClassLevelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private PromotionDecisionType decision;

    @Column(name = "target_class_level_id")
    private UUID targetClassLevelId;

    @Column(name = "target_class_id")
    private UUID targetClassId;

    @Column(name = "justification")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionDecisionStatus status;

    @Column(name = "approved_by")
    private UUID approvedBy;

    protected PromotionDecision() {
    }

    public PromotionDecision(
            UUID promotionRunId,
            UUID studentId,
            UUID sourceClassId,
            UUID sourceClassLevelId,
            PromotionDecisionType decision,
            UUID targetClassLevelId,
            UUID targetClassId) {
        this.promotionRunId = promotionRunId;
        this.studentId = studentId;
        this.sourceClassId = sourceClassId;
        this.sourceClassLevelId = sourceClassLevelId;
        this.decision = decision;
        this.targetClassLevelId = targetClassLevelId;
        this.targetClassId = targetClassId;
        this.status = PromotionDecisionStatus.PROPOSED;
    }

    public UUID getPromotionRunId() {
        return promotionRunId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getSourceClassId() {
        return sourceClassId;
    }

    public UUID getSourceClassLevelId() {
        return sourceClassLevelId;
    }

    public PromotionDecisionType getDecision() {
        return decision;
    }

    public UUID getTargetClassLevelId() {
        return targetClassLevelId;
    }

    public UUID getTargetClassId() {
        return targetClassId;
    }

    public String getJustification() {
        return justification;
    }

    public PromotionDecisionStatus getStatus() {
        return status;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void updateDecision(PromotionDecisionType newDecision, UUID newTargetClassLevelId, String newJustification) {
        this.decision = newDecision;
        this.targetClassLevelId = newTargetClassLevelId;
        this.justification = newJustification;
        this.status = PromotionDecisionStatus.PROPOSED;
    }

    public void assignTargetClass(UUID targetClassId) {
        this.targetClassId = targetClassId;
    }

    public void approve(UUID actorId) {
        this.status = PromotionDecisionStatus.APPROVED;
        this.approvedBy = actorId;
    }

    public void reject(UUID actorId) {
        this.status = PromotionDecisionStatus.REJECTED;
        this.approvedBy = actorId;
    }
}
