package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;

/**
 * Delivers a password-reset OTP to the account's registered phone (primary) or email (FR-AUTH-03,
 * BR-CO-001 SMS-first). Real delivery is the communication module's outbox (ADR-008, WP-8); until
 * that exists, {@link LoggingOtpSender} is the interim implementation.
 */
public interface OtpSender {

    void send(Account account, String otp);
}
