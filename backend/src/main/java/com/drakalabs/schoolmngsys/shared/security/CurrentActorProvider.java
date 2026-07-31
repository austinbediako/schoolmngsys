package com.drakalabs.schoolmngsys.shared.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the account id of whoever is making the current request, for audit and
 * created-by/updated-by attribution. Empty when there is no authenticated actor
 * (system/seed operations, or requests made before WP-1 auth wires a real principal).
 */
public interface CurrentActorProvider {

    Optional<UUID> currentActorId();
}
