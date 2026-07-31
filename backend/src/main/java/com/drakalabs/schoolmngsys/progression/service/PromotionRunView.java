package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.domain.PromotionRun;
import com.drakalabs.schoolmngsys.progression.domain.PromotionRunStatus;
import java.time.Instant;
import java.util.UUID;

public record PromotionRunView(
        UUID id,
        UUID sourceAcademicYearId,
        UUID targetAcademicYearId,
        PromotionRunStatus status,
        Instant executedAt,
        UUID executedBy,
        Instant createdAt
) {

    public static PromotionRunView from(PromotionRun run) {
        return new PromotionRunView(
                run.getId(),
                run.getSourceAcademicYearId(),
                run.getTargetAcademicYearId(),
                run.getStatus(),
                run.getExecutedAt(),
                run.getExecutedBy(),
                run.getCreatedAt()
        );
    }
}
