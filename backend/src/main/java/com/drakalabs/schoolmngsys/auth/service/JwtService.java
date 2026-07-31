package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.config.JwtProperties;
import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Issues and parses short-TTL HS256 access tokens (ADR-004, docs/11 §2). Asymmetric signing is a
 * future-implication of that ADR, not needed while this is the only token consumer.
 */
@Component
public class JwtService {

    private static final String CLAIM_PERSON_TYPE = "personType";
    private static final String CLAIM_PERSON_ID = "personId";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtProperties.expirationMs();
    }

    public String issueAccessToken(Account account, Set<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getId().toString())
                .claim(CLAIM_PERSON_TYPE, account.getPersonType().name())
                .claim(CLAIM_PERSON_ID, account.getPersonId().toString())
                .claim(CLAIM_PERMISSIONS, permissions.stream().sorted().collect(Collectors.toList()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /** @throws JwtException if the token is missing, malformed, expired, or fails signature verification */
    public AccessTokenClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(CLAIM_PERMISSIONS, List.class);

        return new AccessTokenClaims(
                UUID.fromString(claims.getSubject()),
                PersonType.valueOf(claims.get(CLAIM_PERSON_TYPE, String.class)),
                UUID.fromString(claims.get(CLAIM_PERSON_ID, String.class)),
                Set.copyOf(permissions));
    }
}
