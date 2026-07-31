package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.domain.ResultStatus;
import com.drakalabs.schoolmngsys.assessment.service.TermResultView;
import java.math.BigDecimal;
import java.util.UUID;

public record TermResultResponse(
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

    public static TermResultResponse from(TermResultView view) {
        return new TermResultResponse(
                view.id(), view.enrollmentId(), view.classSubjectOfferingId(), view.termId(), view.sbaTotal(), view.examTotal(),
                view.weightedTotal(), view.grade(), view.subjectPosition(), view.status(), view.resultVersion());
    }
}
