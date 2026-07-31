package com.drakalabs.schoolmngsys.shared.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service-layer method whose successful execution must be recorded in {@code audit_log}
 * in the same transaction (BR-SE-002, ADR-007). The {@code audit} module's aspect reacts to this
 * annotation wherever it appears — modules depend only on this marker (in {@code shared}), never
 * on the audit module directly (docs/08 §3: audit is downstream-only).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {

    /** Short verb phrase, e.g. {@code "ENROLLMENT_CREATED"}. */
    String action();

    /** The conceptual entity type being mutated, e.g. {@code "Enrollment"}. */
    String entityType();
}
