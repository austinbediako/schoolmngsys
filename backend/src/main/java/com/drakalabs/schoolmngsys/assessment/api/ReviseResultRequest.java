package com.drakalabs.schoolmngsys.assessment.api;

import jakarta.validation.constraints.NotBlank;

public record ReviseResultRequest(@NotBlank String reason) {
}
