package com.drakalabs.schoolmngsys.auth.api;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {
}
