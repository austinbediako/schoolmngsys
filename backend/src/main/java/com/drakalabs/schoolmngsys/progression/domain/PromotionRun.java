package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotion_runs")
public class PromotionRun extends BaseEntity {

    @Column(name = "source_academic_year_id", nullable = false)
    private UUID sourceAcademicYearId;

    @Column(name = "target_academic_year_id", nullable = false)
    private UUID targetAcademicYearId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionRunStatus status;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "executed_by")
    private UUID executedBy;

    protected PromotionRun() {
    }

    public PromotionRun(UUID sourceAcademicYearId, UUID targetAcademicYearId) {
        this.sourceAcademicYearId = sourceAcademicYearId;
        this.targetAcademicYearId = targetAcademicYearId;
        this.status = PromotionRunStatus.DRAFT;
    }

    public UUID getSourceAcademicYearId() {
        return sourceAcademicYearId;
    }

    public UUID getTargetAcademicYearId() {
        return targetAcademicYearId;
    }

    public PromotionRunStatus getStatus() {
        return status;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public UUID getExecutedBy() {
        return executedBy;
    }

    public void propose() {
        this.status = PromotionRunStatus.PROPOSED;
    }

    public void approve() {
        this.status = PromotionRunStatus.APPROVED;
    }

    public void execute(UUID actorId) {
        this.status = PromotionRunStatus.EXECUTED;
        this.executedAt = Instant.now();
        this.executedBy = actorId;
    }
}
