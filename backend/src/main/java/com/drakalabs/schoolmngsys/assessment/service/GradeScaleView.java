package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.GradeScale;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GradeScaleView(
        UUID id, UUID academicYearId, BigDecimal sbaWeightPercent, BigDecimal examWeightPercent, List<GradeBandView> bands) {

    public static GradeScaleView from(GradeScale scale, List<GradeBandView> bands) {
        return new GradeScaleView(scale.getId(), scale.getAcademicYearId(), scale.getSbaWeightPercent(), scale.getExamWeightPercent(), bands);
    }
}
