package com.drakalabs.schoolmngsys.enrollment.api;

import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** BR-EN-005: reason and date are mandatory for every exit. */
public record RecordExitRequest(@NotNull EnrollmentStatus exitStatus, @NotBlank String reason, @NotNull LocalDate exitDate) {
}
