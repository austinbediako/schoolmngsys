package com.drakalabs.schoolmngsys.communication.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Represents a queued notification in the transactional outbox (ADR-008). */
@Entity
@Table(name = "notification_outbox")
public class OutboxMessage extends BaseEntity {

    @Column(name = "template_code", length = 50)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private MessageChannel channel;

    @Column(name = "recipient_type", length = 20)
    private String recipientType;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    protected OutboxMessage() {
        // JPA constructor
    }

    public OutboxMessage(
            String templateCode,
            MessageChannel channel,
            String recipientType,
            UUID recipientId,
            String recipientPhone,
            String recipientEmail,
            String subject,
            String body,
            int maxRetries) {
        this.templateCode = templateCode;
        this.channel = channel;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.recipientPhone = recipientPhone;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.maxRetries = maxRetries;
        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = Instant.now();
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
        this.lastAttemptAt = Instant.now();
    }

    public void markSent(String providerName, String providerReference) {
        this.status = OutboxStatus.SENT;
        this.providerName = providerName;
        this.providerReference = providerReference;
        this.errorMessage = null;
    }

    public void recordFailure(String errorMessage, Instant nextAttempt) {
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
        this.errorMessage = errorMessage;
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxStatus.FAILED;
            this.nextAttemptAt = null;
        } else {
            this.status = OutboxStatus.PENDING;
            this.nextAttemptAt = nextAttempt;
        }
    }
}
