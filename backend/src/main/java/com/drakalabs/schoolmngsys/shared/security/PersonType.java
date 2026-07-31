package com.drakalabs.schoolmngsys.shared.security;

/**
 * The kind of person record an account links to (docs/03 §1: account ≠ person, ADR-004). Lives
 * in {@code shared} rather than {@code auth} because every module's scope filter needs this
 * discriminator and no module may depend on {@code auth} directly (docs/08 §3).
 */
public enum PersonType {
    STAFF,
    GUARDIAN,
    STUDENT
}
