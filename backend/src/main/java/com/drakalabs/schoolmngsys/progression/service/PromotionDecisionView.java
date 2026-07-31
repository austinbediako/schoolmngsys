package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.domain.PromotionDecision;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionStatus;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionType;
import java.util.UUID;

public record PromotionDecisionView(
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

    public static PromotionDecisionView from(PromotionDecision d) {
        return new PromotionDecisionView(
                d.getId(),
                d.getPromotionRunId(),
                d.getStudentId(),
                d.getSourceClassId(),
                d.getSourceClassLevelId(),
                d.getDecision(),
                d.getTargetClassLevelId(),
                d.getTargetClassId(),
                d.getJustification(),
                d.getStatus(),
                d.getApprovedBy()
        );
    }
}
