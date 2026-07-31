package com.drakalabs.schoolmngsys.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Append-only login ledger backing lockout (FR-AUTH-05) — same rationale as {@code audit_log}:
 * no update/delete path, ever.
 */
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "account_id", updatable = false)
    private UUID accountId;

    @Column(name = "identifier", nullable = false, updatable = false)
    private String identifier;

    @Column(name = "succeeded", nullable = false, updatable = false)
    private boolean succeeded;

    @Column(name = "ip", updatable = false)
    private String ip;

    protected LoginAttempt() {
    }

    public LoginAttempt(UUID accountId, String identifier, boolean succeeded, String ip) {
        this.occurredAt = Instant.now();
        this.accountId = accountId;
        this.identifier = identifier;
        this.succeeded = succeeded;
        this.ip = ip;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
