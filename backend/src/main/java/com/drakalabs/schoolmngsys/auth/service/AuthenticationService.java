package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.config.AuthProperties;
import com.drakalabs.schoolmngsys.auth.config.JwtProperties;
import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountStatus;
import com.drakalabs.schoolmngsys.auth.domain.LoginAttempt;
import com.drakalabs.schoolmngsys.auth.domain.RefreshToken;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.AccountRoleRepository;
import com.drakalabs.schoolmngsys.auth.repository.LoginAttemptRepository;
import com.drakalabs.schoolmngsys.auth.repository.RefreshTokenRepository;
import com.drakalabs.schoolmngsys.people.service.StaffService;
import com.drakalabs.schoolmngsys.people.service.StaffView;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final StaffService staffService;
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
            AccountRoleRepository accountRoleRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginAttemptRepository loginAttemptRepository,
            StaffService staffService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PermissionResolver permissionResolver,
            TokenHasher tokenHasher,
            SecureTokenGenerator tokenGenerator,
            AuthAttemptRecorder authAttemptRecorder,
            AuthProperties authProperties,
            JwtProperties jwtProperties) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.staffService = staffService;
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

    @Transactional(readOnly = true)
    public UserMeView getMe(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("No account for ID " + accountId));

        Set<String> permissions = permissionResolver.resolve(account);
        Set<String> roles = accountRoleRepository.findByAccountIdAndArchivedAtIsNull(account.getId()).stream()
                .map(ar -> ar.getRole().getName())
                .collect(Collectors.toUnmodifiableSet());

        String firstName = "System";
        String lastName = "User";

        if (account.getPersonType() == PersonType.STAFF && account.getPersonId() != null) {
            Optional<StaffView> staffOpt = staffService.findStaffById(account.getPersonId());
            if (staffOpt.isPresent()) {
                firstName = staffOpt.get().firstName();
                lastName = staffOpt.get().lastName();
            }
        }

        return new UserMeView(
                account.getId(),
                account.getLoginIdentifier(),
                firstName,
                lastName,
                account.getEmail(),
                account.getPhone(),
                account.getPersonType() != null ? account.getPersonType().name() : "STAFF",
                roles,
                permissions
        );
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
