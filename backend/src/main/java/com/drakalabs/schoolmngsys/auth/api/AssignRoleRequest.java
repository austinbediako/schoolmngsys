package com.drakalabs.schoolmngsys.auth.api;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequest(@NotBlank String roleName) {
}
