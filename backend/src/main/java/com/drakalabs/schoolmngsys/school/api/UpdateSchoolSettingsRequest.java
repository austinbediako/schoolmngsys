package com.drakalabs.schoolmngsys.school.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateSchoolSettingsRequest(
        @NotBlank String schoolName,
        String motto,
        String address,
        String contactEmail,
        String contactPhone,
        boolean smsNotificationsEnabled,
        boolean emailNotificationsEnabled) {
}
