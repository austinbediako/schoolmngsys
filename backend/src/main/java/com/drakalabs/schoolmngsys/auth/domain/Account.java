package com.drakalabs.schoolmngsys.auth.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A login identity. Links to exactly one Staff/Guardian/Student record via
 * {@code (personType, personId)} (docs/03 §1, ADR-004) — an opaque reference for now since
 * {@code people} (WP-3) doesn't exist yet; no FK, no cross-module entity coupling.
 */
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    private PersonType personType;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "login_identifier", nullable = false)
    private String loginIdentifier;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "force_password_change", nullable = false)
    private boolean forcePasswordChange = true;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    protected Account() {
    }

    public Account(PersonType personType, UUID personId, String loginIdentifier, String phone, String email, String passwordHash) {
        this.personType = personType;
        this.personId = personId;
        this.loginIdentifier = loginIdentifier;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getLoginIdentifier() {
        return loginIdentifier;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePasswordHash(String newHash) {
        this.passwordHash = newHash;
        this.forcePasswordChange = false;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isCurrentlyLocked(Instant now) {
        return status == AccountStatus.LOCKED && lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void recordFailedLogin(int lockoutThreshold, Instant lockoutUntil) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= lockoutThreshold) {
            this.status = AccountStatus.LOCKED;
            this.lockedUntil = lockoutUntil;
        }
    }

    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        if (this.status == AccountStatus.LOCKED) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }

    public void reactivate() {
        this.status = AccountStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
}
