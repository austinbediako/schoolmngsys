package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.ReportCardView;
import java.time.Instant;
import java.util.UUID;

public record ReportCardResponse(
        UUID id,
        UUID enrollmentId,
        UUID termId,
        Integer classPosition,
        String conductRemark,
        String interestRemark,
        String headRemark,
        Instant publishedAt) {

    public static ReportCardResponse from(ReportCardView view) {
        return new ReportCardResponse(
                view.id(), view.enrollmentId(), view.termId(), view.classPosition(), view.conductRemark(), view.interestRemark(),
                view.headRemark(), view.publishedAt());
    }
}
