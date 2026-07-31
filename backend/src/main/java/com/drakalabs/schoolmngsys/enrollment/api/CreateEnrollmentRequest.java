package com.drakalabs.schoolmngsys.enrollment.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEnrollmentRequest(@NotNull UUID studentId, @NotNull UUID classId, @NotNull UUID academicYearId, Integer rollNumber) {
}
