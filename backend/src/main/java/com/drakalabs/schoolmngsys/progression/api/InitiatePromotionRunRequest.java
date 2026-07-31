package com.drakalabs.schoolmngsys.progression.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InitiatePromotionRunRequest(
        @NotNull UUID sourceAcademicYearId,
        @NotNull UUID targetAcademicYearId
) {
}
