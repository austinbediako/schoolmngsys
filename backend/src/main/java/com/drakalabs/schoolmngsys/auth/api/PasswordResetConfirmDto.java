package com.drakalabs.schoolmngsys.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDto(
        @NotBlank String identifier, @NotBlank String otp, @NotBlank @Size(min = 8) String newPassword) {
}
