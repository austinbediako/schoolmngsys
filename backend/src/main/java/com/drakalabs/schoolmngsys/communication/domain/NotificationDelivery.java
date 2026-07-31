package com.drakalabs.schoolmngsys.communication.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Auditable delivery log entry (BR-CO-004, FR-COM-03). */
@Entity
@Table(name = "notification_deliveries")
public class NotificationDelivery extends BaseEntity {

    @Column(name = "outbox_id")
    private UUID outboxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private MessageChannel channel;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "cost", precision = 12, scale = 2)
    private BigDecimal cost;

    protected NotificationDelivery() {
        // JPA constructor
    }

    public NotificationDelivery(
            UUID outboxId,
            MessageChannel channel,
            String recipient,
            String providerName,
            String providerReference,
            DeliveryStatus status,
            Instant attemptedAt,
            String errorMessage,
            BigDecimal cost) {
        this.outboxId = outboxId;
        this.channel = channel;
        this.recipient = recipient;
        this.providerName = providerName;
        this.providerReference = providerReference;
        this.status = status;
        this.attemptedAt = attemptedAt;
        this.errorMessage = errorMessage;
        this.cost = cost;
    }

    public UUID getOutboxId() {
        return outboxId;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public BigDecimal getCost() {
        return cost;
    }
}
