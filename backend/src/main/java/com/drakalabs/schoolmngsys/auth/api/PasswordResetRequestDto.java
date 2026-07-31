package com.drakalabs.schoolmngsys.auth.api;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(@NotBlank String identifier) {
}
