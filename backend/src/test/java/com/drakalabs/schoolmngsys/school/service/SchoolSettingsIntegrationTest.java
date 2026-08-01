package com.drakalabs.schoolmngsys.school.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-11: the V19 seed row is a singleton, shared with every other test in the suite (this
 * container isn't per-test-transactional) — a single sequential test avoids depending on method
 * execution order for a global row that other test classes never touch.
 */
class SchoolSettingsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SchoolSettingsService schoolSettingsService;

    @Test
    void getUpdateAndUpdateLogoAllActOnTheSameSingletonRow() {
        SchoolSettingsView initial = schoolSettingsService.get();
        assertThat(initial.id()).isNotNull();
        assertThat(initial.schoolName()).isNotBlank();

        String newName = "UBS Legon Basic School " + UUID.randomUUID();
        SchoolSettingsView updated = schoolSettingsService.update(
                newName, "Excellence in Basic Education", "New Address", "admin@ubs.edu.gh", "+233200000000", false, true);

        assertThat(updated.id()).isEqualTo(initial.id());
        assertThat(updated.schoolName()).isEqualTo(newName);
        assertThat(updated.motto()).isEqualTo("Excellence in Basic Education");
        assertThat(updated.smsNotificationsEnabled()).isFalse();
        assertThat(updated.emailNotificationsEnabled()).isTrue();

        SchoolSettingsView reread = schoolSettingsService.get();
        assertThat(reread.id()).isEqualTo(initial.id());
        assertThat(reread.schoolName()).isEqualTo(newName);

        SchoolSettingsView withLogo = schoolSettingsService.updateLogo("logo-storage-key-123");
        assertThat(withLogo.id()).isEqualTo(initial.id());
        assertThat(withLogo.logoStorageKey()).isEqualTo("logo-storage-key-123");
        assertThat(withLogo.schoolName()).isEqualTo(newName); // untouched by the logo-only update
    }
}
