package com.drakalabs.schoolmngsys.assessment.service;

import java.math.BigDecimal;
import java.util.UUID;

/** Exactly one of {@code rawScore}, {@code exempted}, {@code naReason} must be set (BR-AA-007). */
public record ScoreEntry(UUID enrollmentId, BigDecimal rawScore, boolean exempted, String naReason) {

    public static ScoreEntry scored(UUID enrollmentId, BigDecimal rawScore) {
        return new ScoreEntry(enrollmentId, rawScore, false, null);
    }

    public static ScoreEntry exempted(UUID enrollmentId) {
        return new ScoreEntry(enrollmentId, null, true, null);
    }

    public static ScoreEntry notApplicable(UUID enrollmentId, String reason) {
        return new ScoreEntry(enrollmentId, null, false, reason);
    }
}
