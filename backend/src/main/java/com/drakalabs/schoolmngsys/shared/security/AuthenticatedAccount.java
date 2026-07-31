package com.drakalabs.schoolmngsys.shared.security;

import java.util.Set;
import java.util.UUID;

/**
 * Who is calling, for scope-filter enforcement (docs/11 §3): a teacher's services filter by own
 * classes, a guardian's by own wards, resolved from {@code personType}/{@code personId} — the
 * concrete resolution logic belongs to each module (people/academics/etc.) once they exist; this
 * is only the mechanism for finding out who is asking.
 */
public record AuthenticatedAccount(UUID accountId, PersonType personType, UUID personId, Set<String> permissions) {

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
