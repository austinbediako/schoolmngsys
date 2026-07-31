package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.GradeBand;
import java.math.BigDecimal;
import java.util.UUID;

public record GradeBandView(UUID id, BigDecimal minScore, BigDecimal maxScore, String grade, String description) {

    public static GradeBandView from(GradeBand band) {
        return new GradeBandView(band.getId(), band.getMinScore(), band.getMaxScore(), band.getGrade(), band.getDescription());
    }
}
