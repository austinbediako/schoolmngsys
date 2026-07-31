package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Permission;
import com.drakalabs.schoolmngsys.auth.domain.Role;
import java.util.List;

/** Entity-free projection of a role — what {@code api} is allowed to see (docs/08 §2). */
public record RoleView(String name, String description, List<String> permissions) {

    public static RoleView from(Role role) {
        return new RoleView(
                role.getName(), role.getDescription(), role.getPermissions().stream().map(Permission::getName).toList());
    }
}
