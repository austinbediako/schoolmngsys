package com.drakalabs.schoolmngsys.people.api;

import jakarta.validation.constraints.NotBlank;

public record AddQualificationRequest(@NotBlank String qualification, String institution, Integer yearObtained) {
}
