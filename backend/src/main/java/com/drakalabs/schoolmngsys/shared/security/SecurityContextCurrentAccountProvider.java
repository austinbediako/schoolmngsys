package com.drakalabs.schoolmngsys.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentAccountProvider implements CurrentAccountProvider {

    @Override
    public Optional<AuthenticatedAccount> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getDetails() instanceof AccountAuthenticationDetails details)) {
            return Optional.empty();
        }

        UUID accountId;
        try {
            accountId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        Set<String> permissions =
                authentication.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .collect(Collectors.toUnmodifiableSet());

        return Optional.of(new AuthenticatedAccount(accountId, details.personType(), details.personId(), permissions));
    }
}
