package com.drakalabs.schoolmngsys.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** A short-lived, single-use, hashed OTP for password reset (FR-AUTH-03). */
@Entity
@Table(name = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "otp_hash", nullable = false, updatable = false)
    private String otpHash;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected PasswordResetOtp() {
    }

    public PasswordResetOtp(UUID accountId, String otpHash, Instant expiresAt) {
        this.accountId = accountId;
        this.otpHash = otpHash;
        this.requestedAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public boolean isUsable(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }

    public void consume() {
        this.consumedAt = Instant.now();
    }
}
