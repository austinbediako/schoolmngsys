package com.drakalabs.schoolmngsys.assessment.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateCustomGradeScaleRequest(
        @NotNull BigDecimal sbaWeightPercent, @NotNull BigDecimal examWeightPercent, @NotEmpty List<@Valid BandRequest> bands) {

    public record BandRequest(
            @NotNull BigDecimal minScore, @NotNull BigDecimal maxScore, @NotBlank String grade, @NotBlank String description) {
    }
}
