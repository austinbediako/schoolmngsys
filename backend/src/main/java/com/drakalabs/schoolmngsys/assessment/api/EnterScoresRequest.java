package com.drakalabs.schoolmngsys.assessment.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EnterScoresRequest(@NotEmpty List<@Valid Entry> entries) {

    /** Exactly one of {@code rawScore}, {@code exempted}, {@code naReason} should be set (BR-AA-007). */
    public record Entry(@NotNull UUID enrollmentId, BigDecimal rawScore, boolean exempted, String naReason) {
    }
}
