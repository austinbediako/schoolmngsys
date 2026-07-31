package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAssessmentComponentRequest(
        @NotNull UUID termId,
        @NotBlank String title,
        @NotNull AssessmentCategory category,
        @NotNull @Positive BigDecimal maxScore,
        @NotNull BigDecimal weightPercent,
        @NotNull LocalDate assessmentDate) {
}
