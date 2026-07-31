package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.GradeBandView;
import java.math.BigDecimal;
import java.util.UUID;

public record GradeBandResponse(UUID id, BigDecimal minScore, BigDecimal maxScore, String grade, String description) {

    public static GradeBandResponse from(GradeBandView view) {
        return new GradeBandResponse(view.id(), view.minScore(), view.maxScore(), view.grade(), view.description());
    }
}
