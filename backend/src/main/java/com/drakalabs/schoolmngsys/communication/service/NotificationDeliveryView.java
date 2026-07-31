package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.DeliveryStatus;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.NotificationDelivery;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record NotificationDeliveryView(
        UUID id,
        UUID outboxId,
        MessageChannel channel,
        String recipient,
        String providerName,
        String providerReference,
        DeliveryStatus status,
        Instant attemptedAt,
        String errorMessage,
        BigDecimal cost
) {
    public static NotificationDeliveryView from(NotificationDelivery delivery) {
        return new NotificationDeliveryView(
                delivery.getId(),
                delivery.getOutboxId(),
                delivery.getChannel(),
                delivery.getRecipient(),
                delivery.getProviderName(),
                delivery.getProviderReference(),
                delivery.getStatus(),
                delivery.getAttemptedAt(),
                delivery.getErrorMessage(),
                delivery.getCost()
        );
    }
}
