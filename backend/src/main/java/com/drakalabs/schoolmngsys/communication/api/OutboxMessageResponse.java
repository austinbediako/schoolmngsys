package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.OutboxStatus;
import com.drakalabs.schoolmngsys.communication.service.OutboxMessageView;
import java.time.Instant;
import java.util.UUID;

public record OutboxMessageResponse(
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
    public static OutboxMessageResponse from(OutboxMessageView view) {
        return new OutboxMessageResponse(
                view.id(),
                view.templateCode(),
                view.channel(),
                view.recipientType(),
                view.recipientId(),
                view.recipientPhone(),
                view.recipientEmail(),
                view.subject(),
                view.body(),
                view.status(),
                view.retryCount(),
                view.maxRetries(),
                view.lastAttemptAt(),
                view.nextAttemptAt(),
                view.errorMessage(),
                view.providerName(),
                view.providerReference()
        );
    }
}
