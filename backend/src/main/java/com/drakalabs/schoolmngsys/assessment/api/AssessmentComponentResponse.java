package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.assessment.service.AssessmentComponentView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssessmentComponentResponse(
        UUID id,
        UUID classSubjectOfferingId,
        UUID termId,
        String title,
        AssessmentCategory category,
        BigDecimal maxScore,
        BigDecimal weightPercent,
        LocalDate assessmentDate) {

    public static AssessmentComponentResponse from(AssessmentComponentView view) {
        return new AssessmentComponentResponse(
                view.id(), view.classSubjectOfferingId(), view.termId(), view.title(), view.category(), view.maxScore(), view.weightPercent(),
                view.assessmentDate());
    }
}
