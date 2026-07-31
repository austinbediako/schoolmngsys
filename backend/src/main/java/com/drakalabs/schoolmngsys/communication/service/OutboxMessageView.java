package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.OutboxMessage;
import com.drakalabs.schoolmngsys.communication.domain.OutboxStatus;
import java.time.Instant;
import java.util.UUID;

public record OutboxMessageView(
        UUID id,
        String templateCode,
        MessageChannel channel,
        String recipientType,
        UUID recipientId,
        String recipientPhone,
        String recipientEmail,
        String subject,
        String body,
        OutboxStatus status,
        int retryCount,
        int maxRetries,
        Instant lastAttemptAt,
        Instant nextAttemptAt,
        String errorMessage,
        String providerName,
        String providerReference
) {
    public static OutboxMessageView from(OutboxMessage outbox) {
        return new OutboxMessageView(
                outbox.getId(),
                outbox.getTemplateCode(),
                outbox.getChannel(),
                outbox.getRecipientType(),
                outbox.getRecipientId(),
                outbox.getRecipientPhone(),
                outbox.getRecipientEmail(),
                outbox.getSubject(),
                outbox.getBody(),
                outbox.getStatus(),
                outbox.getRetryCount(),
                outbox.getMaxRetries(),
                outbox.getLastAttemptAt(),
                outbox.getNextAttemptAt(),
                outbox.getErrorMessage(),
                outbox.getProviderName(),
                outbox.getProviderReference()
        );
    }
}
