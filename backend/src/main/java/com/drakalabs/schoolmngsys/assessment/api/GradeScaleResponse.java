package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.GradeScaleView;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GradeScaleResponse(
        UUID id, UUID academicYearId, BigDecimal sbaWeightPercent, BigDecimal examWeightPercent, List<GradeBandResponse> bands) {

    public static GradeScaleResponse from(GradeScaleView view) {
        return new GradeScaleResponse(
                view.id(),
                view.academicYearId(),
                view.sbaWeightPercent(),
                view.examWeightPercent(),
                view.bands().stream().map(GradeBandResponse::from).toList());
    }
}
