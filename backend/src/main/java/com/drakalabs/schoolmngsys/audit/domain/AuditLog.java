package com.drakalabs.schoolmngsys.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * Append-only audit trail (docs/09 §6, ADR-007). No FK to domain tables — audit outlives
 * archived data — and no update/delete path exists anywhere in the application.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "actor_account_id", updatable = false)
    private UUID actorAccountId;

    @Column(name = "action", nullable = false, updatable = false)
    private String action;

    @Column(name = "entity_type", nullable = false, updatable = false)
    private String entityType;

    @Column(name = "entity_id", updatable = false)
    private String entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary", updatable = false)
    private Map<String, Object> summary;

    @Column(name = "ip", updatable = false)
    private String ip;

    protected AuditLog() {
    }

    public AuditLog(
            UUID actorAccountId,
            String action,
            String entityType,
            String entityId,
            Map<String, Object> summary,
            String ip) {
        this.occurredAt = Instant.now();
        this.actorAccountId = actorAccountId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.summary = summary;
        this.ip = ip;
    }

    public UUID getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getActorAccountId() {
        return actorAccountId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public String getIp() {
        return ip;
    }
}
