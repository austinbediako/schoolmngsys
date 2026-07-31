package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.domain.PromotionRunStatus;
import com.drakalabs.schoolmngsys.progression.service.PromotionRunView;
import java.time.Instant;
import java.util.UUID;

public record PromotionRunResponse(
        UUID id,
        UUID sourceAcademicYearId,
        UUID targetAcademicYearId,
        PromotionRunStatus status,
        Instant executedAt,
        UUID executedBy,
        Instant createdAt
) {

    public static PromotionRunResponse from(PromotionRunView view) {
        return new PromotionRunResponse(
                view.id(),
                view.sourceAcademicYearId(),
                view.targetAcademicYearId(),
                view.status(),
                view.executedAt(),
                view.executedBy(),
                view.createdAt()
        );
    }
}
