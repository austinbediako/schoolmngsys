package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.LoginAttempt;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.LoginAttemptRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records login attempts (and lockout state) in their own transaction. {@code login()} always
 * ends a denied attempt by throwing an {@code AuthenticationException} — an unchecked exception
 * that would otherwise roll back everything the same transactional method just wrote (the
 * failed-attempt counter, the lockout, the ledger entry). {@code REQUIRES_NEW} commits those
 * writes independently before the caller's exception propagates.
 */
@Component
public class AuthAttemptRecorder {

    private final AccountRepository accountRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    public AuthAttemptRecorder(AccountRepository accountRepository, LoginAttemptRepository loginAttemptRepository) {
        this.accountRepository = accountRepository;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(
            Account account, String identifier, String ip, int lockoutThreshold, long lockoutDurationMs) {
        if (account != null) {
            account.recordFailedLogin(lockoutThreshold, Instant.now().plusMillis(lockoutDurationMs));
            accountRepository.save(account);
        }
        loginAttemptRepository.save(new LoginAttempt(account == null ? null : account.getId(), identifier, false, ip));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDeniedAttempt(Account account, String identifier, String ip) {
        loginAttemptRepository.save(new LoginAttempt(account.getId(), identifier, false, ip));
    }
}
