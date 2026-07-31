package com.drakalabs.schoolmngsys.academics.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateSubjectOfferingRequest(@NotNull UUID subjectId, @NotNull UUID academicYearId) {
}
