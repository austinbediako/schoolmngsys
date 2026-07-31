package com.drakalabs.schoolmngsys.communication.service.provider;

/** Pluggable Email provider interface (ADR-008). */
public interface EmailAdapter {
    EmailSendResult sendEmail(String recipientEmail, String subject, String message);
    String providerName();
}
