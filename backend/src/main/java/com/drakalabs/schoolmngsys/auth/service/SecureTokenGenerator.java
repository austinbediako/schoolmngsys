package com.drakalabs.schoolmngsys.auth.service;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureTokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    /** A 256-bit random value, URL-safe encoded — used raw for refresh tokens (only the hash is stored). */
    public String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    /** A 6-digit numeric OTP, suitable for SMS delivery (FR-AUTH-03). */
    public String generateNumericOtp() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
