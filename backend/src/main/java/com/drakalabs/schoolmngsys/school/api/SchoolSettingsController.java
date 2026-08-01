package com.drakalabs.schoolmngsys.school.api;

import com.drakalabs.schoolmngsys.school.service.SchoolSettingsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school-settings")
public class SchoolSettingsController {

    private final SchoolSettingsService schoolSettingsService;

    public SchoolSettingsController(SchoolSettingsService schoolSettingsService) {
        this.schoolSettingsService = schoolSettingsService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCHOOL_SETTINGS_VIEW')")
    public SchoolSettingsResponse get() {
        return SchoolSettingsResponse.from(schoolSettingsService.get());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SCHOOL_SETTINGS_MANAGE')")
    public SchoolSettingsResponse update(@RequestBody @Valid UpdateSchoolSettingsRequest request) {
        return SchoolSettingsResponse.from(
                schoolSettingsService.update(
                        request.schoolName(),
                        request.motto(),
                        request.address(),
                        request.contactEmail(),
                        request.contactPhone(),
                        request.smsNotificationsEnabled(),
                        request.emailNotificationsEnabled()));
    }

    @PutMapping("/logo")
    @PreAuthorize("hasAuthority('SCHOOL_SETTINGS_MANAGE')")
    public SchoolSettingsResponse updateLogo(@RequestBody @Valid UpdateSchoolLogoRequest request) {
        return SchoolSettingsResponse.from(schoolSettingsService.updateLogo(request.logoStorageKey()));
    }
}
