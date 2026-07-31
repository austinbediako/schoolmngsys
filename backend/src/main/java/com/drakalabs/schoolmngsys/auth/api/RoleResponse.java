package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.RoleView;
import java.util.List;

public record RoleResponse(String name, String description, List<String> permissions) {

    public static RoleResponse from(RoleView view) {
        return new RoleResponse(view.name(), view.description(), view.permissions());
    }
}
