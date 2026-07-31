package com.drakalabs.schoolmngsys.shared.security;

import java.util.UUID;

/**
 * What {@code auth}'s JWT authentication filter attaches as {@code Authentication.getDetails()}
 * so {@link SecurityContextCurrentAccountProvider} can read it without depending on {@code auth}.
 */
public record AccountAuthenticationDetails(PersonType personType, UUID personId) {
}
