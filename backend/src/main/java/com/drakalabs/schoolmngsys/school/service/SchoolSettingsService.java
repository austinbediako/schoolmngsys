package com.drakalabs.schoolmngsys.school.service;

import com.drakalabs.schoolmngsys.school.domain.SchoolSettings;
import com.drakalabs.schoolmngsys.school.repository.SchoolSettingsRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The single {@link SchoolSettings} row is seeded by V17 — this service only ever reads/updates
 * that one row, never creates a second (the partial unique index would reject it anyway).
 */
@Service
public class SchoolSettingsService {

    private final SchoolSettingsRepository schoolSettingsRepository;

    public SchoolSettingsService(SchoolSettingsRepository schoolSettingsRepository) {
        this.schoolSettingsRepository = schoolSettingsRepository;
    }

    @Transactional(readOnly = true)
    public SchoolSettingsView get() {
        return SchoolSettingsView.from(getSettings());
    }

    @Audited(action = "SCHOOL_SETTINGS_UPDATED", entityType = "SchoolSettings")
    @Transactional
    public SchoolSettingsView update(
            String schoolName,
            String motto,
            String address,
            String contactEmail,
            String contactPhone,
            boolean smsNotificationsEnabled,
            boolean emailNotificationsEnabled) {
        SchoolSettings settings = getSettings();
        settings.update(schoolName, motto, address, contactEmail, contactPhone, smsNotificationsEnabled, emailNotificationsEnabled);
        return SchoolSettingsView.from(schoolSettingsRepository.save(settings));
    }

    @Audited(action = "SCHOOL_SETTINGS_LOGO_UPDATED", entityType = "SchoolSettings")
    @Transactional
    public SchoolSettingsView updateLogo(String logoStorageKey) {
        SchoolSettings settings = getSettings();
        settings.updateLogo(logoStorageKey);
        return SchoolSettingsView.from(schoolSettingsRepository.save(settings));
    }

    private SchoolSettings getSettings() {
        return schoolSettingsRepository
                .findFirstByArchivedAtIsNull()
                .orElseThrow(() -> new NotFoundException("School settings row is missing — check the V17 seed migration"));
    }
}
