package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/** BR-FI-001: one per (ClassLevel, Term), must be APPROVED (by the Head) before it can be billed. */
@Entity
@Table(name = "fee_schedules")
public class FeeSchedule extends BaseEntity {

    @Column(name = "class_level_id", nullable = false, updatable = false)
    private UUID classLevelId;

    @Column(name = "term_id", nullable = false, updatable = false)
    private UUID termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeeScheduleStatus status = FeeScheduleStatus.DRAFT;

    protected FeeSchedule() {
    }

    public FeeSchedule(UUID classLevelId, UUID termId) {
        this.classLevelId = classLevelId;
        this.termId = termId;
    }

    public UUID getClassLevelId() {
        return classLevelId;
    }

    public UUID getTermId() {
        return termId;
    }

    public FeeScheduleStatus getStatus() {
        return status;
    }

    public void approve() {
        this.status = FeeScheduleStatus.APPROVED;
    }
}
