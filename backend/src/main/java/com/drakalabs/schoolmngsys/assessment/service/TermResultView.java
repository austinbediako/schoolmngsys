package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.ResultStatus;
import com.drakalabs.schoolmngsys.assessment.domain.TermResult;
import java.math.BigDecimal;
import java.util.UUID;

public record TermResultView(
        UUID id,
        UUID enrollmentId,
        UUID classSubjectOfferingId,
        UUID termId,
        BigDecimal sbaTotal,
        BigDecimal examTotal,
        BigDecimal weightedTotal,
        String grade,
        Integer subjectPosition,
        ResultStatus status,
        int resultVersion) {

    public static TermResultView from(TermResult result) {
        return new TermResultView(
                result.getId(),
                result.getEnrollmentId(),
                result.getClassSubjectOfferingId(),
                result.getTermId(),
                result.getSbaTotal(),
                result.getExamTotal(),
                result.getWeightedTotal(),
                result.getGrade(),
                result.getSubjectPosition(),
                result.getStatus(),
                result.getResultVersion());
    }
}
