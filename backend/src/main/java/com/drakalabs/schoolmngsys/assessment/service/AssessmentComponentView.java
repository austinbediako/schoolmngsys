package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssessmentComponentView(
        UUID id,
        UUID classSubjectOfferingId,
        UUID termId,
        String title,
        AssessmentCategory category,
        BigDecimal maxScore,
        BigDecimal weightPercent,
        LocalDate assessmentDate) {

    public static AssessmentComponentView from(AssessmentComponent component) {
        return new AssessmentComponentView(
                component.getId(),
                component.getClassSubjectOfferingId(),
                component.getTermId(),
                component.getTitle(),
                component.getCategory(),
                component.getMaxScore(),
                component.getWeightPercent(),
                component.getAssessmentDate());
    }
}
