package com.drakalabs.schoolmngsys.shared.event;

import java.time.Instant;

/**
 * Base for cross-module notification events (docs/02 §4). Naming convention: past tense,
 * {@code <Entity><Happened>}. Consumers subscribe with {@code @EventListener} or
 * {@code @TransactionalEventListener} — never called directly across module boundaries.
 */
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();

    public Instant occurredAt() {
        return occurredAt;
    }
}
