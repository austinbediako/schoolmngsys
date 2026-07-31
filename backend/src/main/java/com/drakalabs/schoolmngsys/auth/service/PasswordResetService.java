package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.config.AuthProperties;
import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.PasswordResetOtp;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.PasswordResetOtpRepository;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Password reset via OTP (FR-AUTH-03) — no security questions, per docs/03 §5. */
@Service
public class PasswordResetService {

    private final AccountRepository accountRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHasher tokenHasher;
    private final SecureTokenGenerator tokenGenerator;
    private final OtpSender otpSender;
    private final AuthProperties authProperties;

    public PasswordResetService(
            AccountRepository accountRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            PasswordEncoder passwordEncoder,
            TokenHasher tokenHasher,
            SecureTokenGenerator tokenGenerator,
            OtpSender otpSender,
            AuthProperties authProperties) {
        this.accountRepository = accountRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenHasher = tokenHasher;
        this.tokenGenerator = tokenGenerator;
        this.otpSender = otpSender;
        this.authProperties = authProperties;
    }

    /** Silently no-ops for an unknown identifier — a reset request must never reveal account existence. */
    @Transactional
    public void requestReset(String identifier) {
        accountRepository
                .findByLoginIdentifierAndArchivedAtIsNull(identifier)
                .ifPresent(
                        account -> {
                            String otp = tokenGenerator.generateNumericOtp();
                            Instant expiresAt = Instant.now().plusMillis(authProperties.otpExpirationMs());
                            passwordResetOtpRepository.save(
                                    new PasswordResetOtp(account.getId(), tokenHasher.hash(otp), expiresAt));
                            otpSender.send(account, otp);
                        });
    }

    @Transactional
    public void confirmReset(String identifier, String otp, String newPassword) {
        Account account = accountRepository
                .findByLoginIdentifierAndArchivedAtIsNull(identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid identifier or OTP"));

        String otpHash = tokenHasher.hash(otp);
        Instant now = Instant.now();
        PasswordResetOtp matching = passwordResetOtpRepository.findByAccountIdAndConsumedAtIsNull(account.getId())
                .stream()
                .filter(candidate -> candidate.isUsable(now) && candidate.getOtpHash().equals(otpHash))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Invalid identifier or OTP"));

        matching.consume();
        passwordResetOtpRepository.save(matching);

        account.changePasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
