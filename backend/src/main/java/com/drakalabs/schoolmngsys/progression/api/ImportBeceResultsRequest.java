package com.drakalabs.schoolmngsys.progression.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ImportBeceResultsRequest(
        @NotEmpty List<SubjectScore> scores
) {

    public record SubjectScore(
            @NotNull UUID subjectId,
            @Min(1) @Max(9) int grade
    ) {
    }
}
