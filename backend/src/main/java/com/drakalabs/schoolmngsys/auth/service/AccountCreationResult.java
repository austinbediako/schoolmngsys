package com.drakalabs.schoolmngsys.auth.service;

/**
 * The provisioned account plus its one-time temporary password. Delivering this to the person
 * (SMS/email) is the communication module's job (WP-8, ADR-008); until then the caller (API
 * layer) is responsible for surfacing it, since there is no outbox to hand it to yet.
 */
public record AccountCreationResult(AccountView account, String temporaryPassword) {
}
