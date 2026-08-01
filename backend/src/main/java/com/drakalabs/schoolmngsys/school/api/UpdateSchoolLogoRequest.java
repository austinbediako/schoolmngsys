package com.drakalabs.schoolmngsys.school.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateSchoolLogoRequest(@NotBlank String logoStorageKey) {
}
