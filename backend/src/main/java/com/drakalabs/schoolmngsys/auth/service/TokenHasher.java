package com.drakalabs.schoolmngsys.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * SHA-256 hashing for high-entropy secrets we only ever compare, never recover (refresh tokens,
 * OTPs) — a slow password hash (bcrypt/argon2) is unnecessary and slower than needed here.
 */
@Component
public class TokenHasher {

    public String hash(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
