package com.drakalabs.schoolmngsys.people.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateStudentBioRequest(@NotBlank String firstName, @NotBlank String lastName, String otherNames) {
}
