package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.ScoreView;
import java.math.BigDecimal;
import java.util.UUID;

public record ScoreResponse(UUID id, UUID assessmentComponentId, UUID enrollmentId, BigDecimal rawScore, boolean exempted, String naReason) {

    public static ScoreResponse from(ScoreView view) {
        return new ScoreResponse(view.id(), view.assessmentComponentId(), view.enrollmentId(), view.rawScore(), view.exempted(), view.naReason());
    }
}
