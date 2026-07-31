package com.drakalabs.schoolmngsys.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

/**
 * WP-1 test plan (docs/14 §5): token issue/refresh/rotation; revocation on deactivation
 * (FR-AUTH-04); lockout (FR-AUTH-05).
 */
class AuthenticationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AccountProvisioningService accountProvisioningService;

    @Autowired
    private AuthenticationService authenticationService;

    private AccountCreationResult provisionAccount(String identifier) {
        return accountProvisioningService.createAccount(
                PersonType.STAFF, UUID.randomUUID(), identifier, uniquePhone(), null);
    }

    private String uniquePhone() {
        return "+2332" + String.format("%09d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000));
    }

    @Test
    void loginWithCorrectCredentialsIssuesAccessAndRefreshTokens() {
        AccountCreationResult provisioned = provisionAccount("teacher.login.ok");

        AuthTokens tokens =
                authenticationService.login("teacher.login.ok", provisioned.temporaryPassword(), "127.0.0.1");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.expiresInSeconds()).isPositive();
    }

    @Test
    void loginWithWrongPasswordFails() {
        provisionAccount("teacher.login.badpw");

        assertThatThrownBy(() -> authenticationService.login("teacher.login.badpw", "wrong-password", "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void repeatedFailedLoginsLockTheAccount() {
        AccountCreationResult provisioned = provisionAccount("teacher.login.lockout");

        for (int i = 0; i < 5; i++) {
            try {
                authenticationService.login("teacher.login.lockout", "wrong-password", "127.0.0.1");
            } catch (BadCredentialsException ignored) {
                // expected until lockout kicks in
            }
        }

        assertThatThrownBy(
                        () -> authenticationService.login(
                                "teacher.login.lockout", provisioned.temporaryPassword(), "127.0.0.1"))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void refreshRotatesTheRefreshTokenAndInvalidatesTheOldOne() {
        AccountCreationResult provisioned = provisionAccount("teacher.login.rotate");
        AuthTokens initial =
                authenticationService.login("teacher.login.rotate", provisioned.temporaryPassword(), "127.0.0.1");

        AuthTokens rotated = authenticationService.refresh(initial.refreshToken());

        assertThat(rotated.refreshToken()).isNotEqualTo(initial.refreshToken());
        assertThatThrownBy(() -> authenticationService.refresh(initial.refreshToken()))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deactivationRevokesRefreshCapabilityImmediately() {
        AccountCreationResult provisioned = provisionAccount("teacher.login.deactivate");
        AuthTokens tokens =
                authenticationService.login("teacher.login.deactivate", provisioned.temporaryPassword(), "127.0.0.1");

        accountProvisioningService.deactivateAccount(provisioned.account().id());

        assertThatThrownBy(() -> authenticationService.refresh(tokens.refreshToken()))
                .isInstanceOf(DisabledException.class);
    }
}
