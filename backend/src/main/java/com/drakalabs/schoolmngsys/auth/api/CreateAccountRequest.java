package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.shared.security.PersonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAccountRequest(
        @NotNull PersonType personType,
        @NotNull UUID personId,
        @NotBlank String loginIdentifier,
        String phone,
        String email) {
}
