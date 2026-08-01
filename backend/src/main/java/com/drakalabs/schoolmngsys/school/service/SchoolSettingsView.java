package com.drakalabs.schoolmngsys.school.service;

import com.drakalabs.schoolmngsys.school.domain.SchoolSettings;
import java.util.UUID;

public record SchoolSettingsView(
        UUID id,
        String schoolName,
        String motto,
        String address,
        String contactEmail,
        String contactPhone,
        String logoStorageKey,
        boolean smsNotificationsEnabled,
        boolean emailNotificationsEnabled) {

    public static SchoolSettingsView from(SchoolSettings settings) {
        return new SchoolSettingsView(
                settings.getId(),
                settings.getSchoolName(),
                settings.getMotto(),
                settings.getAddress(),
                settings.getContactEmail(),
                settings.getContactPhone(),
                settings.getLogoStorageKey(),
                settings.isSmsNotificationsEnabled(),
                settings.isEmailNotificationsEnabled());
    }
}
