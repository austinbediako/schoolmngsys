package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.Set;
import java.util.UUID;

/** What the access token carries — everything a service needs to authorize + scope a request. */
public record AccessTokenClaims(UUID accountId, PersonType personType, UUID personId, Set<String> permissions) {
}
