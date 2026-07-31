package com.drakalabs.schoolmngsys.shared.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the actor from Spring Security's context. WP-1 must authenticate requests with a
 * principal whose {@code getName()} returns the account id as a UUID string (e.g. the JWT
 * {@code sub} claim) — this is the contract every module's audit/attribution wiring assumes.
 */
@Component
public class SecurityContextCurrentActorProvider implements CurrentActorProvider {

    @Override
    public Optional<UUID> currentActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(authentication.getName()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
