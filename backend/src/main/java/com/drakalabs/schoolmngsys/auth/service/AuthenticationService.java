package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.config.AuthProperties;
import com.drakalabs.schoolmngsys.auth.config.JwtProperties;
import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountStatus;
import com.drakalabs.schoolmngsys.auth.domain.LoginAttempt;
import com.drakalabs.schoolmngsys.auth.domain.RefreshToken;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.LoginAttemptRepository;
import com.drakalabs.schoolmngsys.auth.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login, refresh rotation, and logout (FR-AUTH-01/04/05, ADR-004). Failure cases throw Spring
 * Security {@link org.springframework.security.core.AuthenticationException} subtypes so they
 * flow straight into the WP-0 {@code GlobalExceptionHandler}'s existing auth-required mapping —
 * no new problem-type wiring needed here.
 *
 * <p>Denied-attempt bookkeeping (lockout counter, ledger) goes through {@link AuthAttemptRecorder}
 * in its own transaction: every denial path here ends by throwing an unchecked
 * {@code AuthenticationException}, which would otherwise roll back whatever this method just
 * wrote before the exception propagates.
 */
@Service
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PermissionResolver permissionResolver;
    private final TokenHasher tokenHasher;
    private final SecureTokenGenerator tokenGenerator;
    private final AuthAttemptRecorder authAttemptRecorder;
    private final int lockoutThreshold;
    private final long lockoutDurationMs;
    private final long refreshExpirationMs;
    private final long accessExpirationMs;

    public AuthenticationService(
            AccountRepository accountRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginAttemptRepository loginAttemptRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PermissionResolver permissionResolver,
            TokenHasher tokenHasher,
            SecureTokenGenerator tokenGenerator,
            AuthAttemptRecorder authAttemptRecorder,
            AuthProperties authProperties,
            JwtProperties jwtProperties) {
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.permissionResolver = permissionResolver;
        this.tokenHasher = tokenHasher;
        this.tokenGenerator = tokenGenerator;
        this.authAttemptRecorder = authAttemptRecorder;
        this.lockoutThreshold = authProperties.lockoutThreshold();
        this.lockoutDurationMs = authProperties.lockoutDurationMs();
        this.refreshExpirationMs = jwtProperties.refreshExpirationMs();
        this.accessExpirationMs = jwtProperties.expirationMs();
    }

    @Transactional
    public AuthTokens login(String identifier, String rawPassword, String ip) {
        Account account = accountRepository.findByLoginIdentifierAndArchivedAtIsNull(identifier).orElse(null);

        if (account == null || !passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            authAttemptRecorder.recordFailedAttempt(account, identifier, ip, lockoutThreshold, lockoutDurationMs);
            throw new BadCredentialsException("Invalid credentials");
        }

        Instant now = Instant.now();
        if (account.isCurrentlyLocked(now)) {
            authAttemptRecorder.recordDeniedAttempt(account, identifier, ip);
            throw new LockedException("Account is locked, try again later");
        }
        if (account.getStatus() == AccountStatus.DEACTIVATED) {
            authAttemptRecorder.recordDeniedAttempt(account, identifier, ip);
            throw new DisabledException("Account is deactivated");
        }

        account.recordSuccessfulLogin();
        accountRepository.save(account);
        loginAttemptRepository.save(new LoginAttempt(account.getId(), identifier, true, ip));

        return issueTokenPair(account);
    }

    @Transactional
    public AuthTokens refresh(String rawRefreshToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));

        Account account = current.getAccount();
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("Account is deactivated");
        }
        if (!current.isUsable(Instant.now())) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        AuthTokens tokens = issueTokenPair(account);
        RefreshToken newToken = refreshTokenRepository.findByTokenHash(tokenHasher.hash(tokens.refreshToken()))
                .orElseThrow();
        current.revoke(newToken.getId());
        refreshTokenRepository.save(current);

        return tokens;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken))
                .ifPresent(token -> {
                    token.revoke(null);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthTokens issueTokenPair(Account account) {
        Set<String> permissions = permissionResolver.resolve(account);
        String accessToken = jwtService.issueAccessToken(account, permissions);

        String rawRefreshToken = tokenGenerator.generateOpaqueToken();
        Instant now = Instant.now();
        RefreshToken refreshToken = new RefreshToken(
                account, tokenHasher.hash(rawRefreshToken), now, now.plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(refreshToken);

        return new AuthTokens(accessToken, rawRefreshToken, accessExpirationMs / 1000);
    }
}
