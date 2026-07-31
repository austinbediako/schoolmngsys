package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.DeliveryStatus;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.service.NotificationDeliveryView;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record NotificationDeliveryResponse(
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
    public static NotificationDeliveryResponse from(NotificationDeliveryView view) {
        return new NotificationDeliveryResponse(
                view.id(),
                view.outboxId(),
                view.channel(),
                view.recipient(),
                view.providerName(),
                view.providerReference(),
                view.status(),
                view.attemptedAt(),
                view.errorMessage(),
                view.cost()
        );
    }
}
