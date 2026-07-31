package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.Score;
import java.math.BigDecimal;
import java.util.UUID;

public record ScoreView(UUID id, UUID assessmentComponentId, UUID enrollmentId, BigDecimal rawScore, boolean exempted, String naReason) {

    public static ScoreView from(Score score) {
        return new ScoreView(
                score.getId(), score.getAssessmentComponentId(), score.getEnrollmentId(), score.getRawScore(), score.isExempted(), score.getNaReason());
    }
}
