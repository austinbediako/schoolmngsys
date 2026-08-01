package com.drakalabs.schoolmngsys.school.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * WP-11: a single configuration row for the whole school (name, contact/branding, notification
 * toggles) — never a second row. Enforced by a partial unique index on a constant expression
 * (V17 migration), the same technique used elsewhere in this schema for "at most one" invariants.
 * Academic/grading/attendance settings are NOT duplicated here — those already have real owning
 * homes (GradeScale per academic year, school-day calendar, fee schedules) and repeating them here
 * would create two sources of truth.
 */
@Entity
@Table(name = "school_settings")
public class SchoolSettings extends BaseEntity {

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "motto")
    private String motto;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "logo_storage_key")
    private String logoStorageKey;

    @Column(name = "sms_notifications_enabled", nullable = false)
    private boolean smsNotificationsEnabled = true;

    @Column(name = "email_notifications_enabled", nullable = false)
    private boolean emailNotificationsEnabled = true;

    protected SchoolSettings() {
    }

    public SchoolSettings(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getMotto() {
        return motto;
    }

    public String getAddress() {
        return address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getLogoStorageKey() {
        return logoStorageKey;
    }

    public boolean isSmsNotificationsEnabled() {
        return smsNotificationsEnabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void update(
            String schoolName,
            String motto,
            String address,
            String contactEmail,
            String contactPhone,
            boolean smsNotificationsEnabled,
            boolean emailNotificationsEnabled) {
        this.schoolName = schoolName;
        this.motto = motto;
        this.address = address;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.smsNotificationsEnabled = smsNotificationsEnabled;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public void updateLogo(String logoStorageKey) {
        this.logoStorageKey = logoStorageKey;
    }
}
