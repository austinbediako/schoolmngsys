package com.drakalabs.schoolmngsys.people.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateStaffBioRequest(@NotBlank String firstName, @NotBlank String lastName, String gesRegistrationNumber) {
}
