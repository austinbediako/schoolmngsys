package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionStatus;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionType;
import com.drakalabs.schoolmngsys.progression.service.PromotionDecisionView;
import java.util.UUID;

public record PromotionDecisionResponse(
        UUID id,
        UUID promotionRunId,
        UUID studentId,
        UUID sourceClassId,
        UUID sourceClassLevelId,
        PromotionDecisionType decision,
        UUID targetClassLevelId,
        UUID targetClassId,
        String justification,
        PromotionDecisionStatus status,
        UUID approvedBy
) {

    public static PromotionDecisionResponse from(PromotionDecisionView view) {
        return new PromotionDecisionResponse(
                view.id(),
                view.promotionRunId(),
                view.studentId(),
                view.sourceClassId(),
                view.sourceClassLevelId(),
                view.decision(),
                view.targetClassLevelId(),
                view.targetClassId(),
                view.justification(),
                view.status(),
                view.approvedBy()
        );
    }
}
