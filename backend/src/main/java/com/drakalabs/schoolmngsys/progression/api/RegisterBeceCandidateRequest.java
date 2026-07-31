package com.drakalabs.schoolmngsys.progression.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterBeceCandidateRequest(
        @NotNull UUID enrollmentId,
        @NotBlank String indexNumber
) {
}
