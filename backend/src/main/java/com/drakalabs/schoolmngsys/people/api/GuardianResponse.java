package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.GuardianView;
import java.util.UUID;

public record GuardianResponse(UUID id, String firstName, String lastName, String phone, String email, String occupation, String address) {

    public static GuardianResponse from(GuardianView view) {
        return new GuardianResponse(view.id(), view.firstName(), view.lastName(), view.phone(), view.email(), view.occupation(), view.address());
    }
}
