package com.drakalabs.schoolmngsys.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * The one seam every module publishes domain events through (in-process, per docs/02 §4).
 * External effects (SMS, email) are the communication module's job via the outbox (ADR-008),
 * subscribing to these events rather than being called synchronously.
 */
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public DomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
