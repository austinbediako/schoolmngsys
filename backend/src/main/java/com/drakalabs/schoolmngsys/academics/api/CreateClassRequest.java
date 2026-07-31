package com.drakalabs.schoolmngsys.academics.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateClassRequest(@NotBlank String classLevelCode, @NotBlank String stream, @Min(1) int capacity) {
}
