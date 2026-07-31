package com.drakalabs.schoolmngsys.people.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EndEmploymentRequest(@NotNull LocalDate endDate) {
}
