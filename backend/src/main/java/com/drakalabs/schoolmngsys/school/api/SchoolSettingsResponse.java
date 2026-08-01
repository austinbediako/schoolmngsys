package com.drakalabs.schoolmngsys.school.api;

import com.drakalabs.schoolmngsys.school.service.SchoolSettingsView;
import java.util.UUID;

public record SchoolSettingsResponse(
        UUID id,
        String schoolName,
        String motto,
        String address,
        String contactEmail,
        String contactPhone,
        String logoStorageKey,
        boolean smsNotificationsEnabled,
        boolean emailNotificationsEnabled) {

    public static SchoolSettingsResponse from(SchoolSettingsView view) {
        return new SchoolSettingsResponse(
                view.id(),
                view.schoolName(),
                view.motto(),
                view.address(),
                view.contactEmail(),
                view.contactPhone(),
                view.logoStorageKey(),
                view.smsNotificationsEnabled(),
                view.emailNotificationsEnabled());
    }
}
