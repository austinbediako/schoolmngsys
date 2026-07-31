package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProposeDecisionExceptionRequest(
        @NotNull PromotionDecisionType decisionType,
        UUID targetClassLevelId,
        String justification
) {
}
