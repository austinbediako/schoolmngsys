package com.drakalabs.schoolmngsys.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/** WP-1 test plan (docs/14 §5): "scope-filter unit tests" — the mechanism every module builds on. */
class SecurityContextCurrentAccountProviderTest {

    private final SecurityContextCurrentAccountProvider provider = new SecurityContextCurrentAccountProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsEmptyWhenNotAuthenticated() {
        assertThat(provider.current()).isEmpty();
    }

    @Test
    void resolvesAccountIdPersonAndPermissionsFromTheSecurityContext() {
        UUID accountId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(
                accountId.toString(), null, List.of(new SimpleGrantedAuthority("STUDENT_VIEW")));
        authentication.setDetails(new AccountAuthenticationDetails(PersonType.GUARDIAN, personId));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AuthenticatedAccount current = provider.current().orElseThrow();

        assertThat(current.accountId()).isEqualTo(accountId);
        assertThat(current.personType()).isEqualTo(PersonType.GUARDIAN);
        assertThat(current.personId()).isEqualTo(personId);
        assertThat(current.hasPermission("STUDENT_VIEW")).isTrue();
        assertThat(current.hasPermission("PAYMENT_RECORD")).isFalse();
    }
}
