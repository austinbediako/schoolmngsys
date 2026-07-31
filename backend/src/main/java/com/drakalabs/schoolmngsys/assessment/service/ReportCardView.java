package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.ReportCard;
import java.time.Instant;
import java.util.UUID;

public record ReportCardView(
        UUID id,
        UUID enrollmentId,
        UUID termId,
        Integer classPosition,
        String conductRemark,
        String interestRemark,
        String headRemark,
        Instant publishedAt) {

    public static ReportCardView from(ReportCard card) {
        return new ReportCardView(
                card.getId(),
                card.getEnrollmentId(),
                card.getTermId(),
                card.getClassPosition(),
                card.getConductRemark(),
                card.getInterestRemark(),
                card.getHeadRemark(),
                card.getPublishedAt());
    }
}
