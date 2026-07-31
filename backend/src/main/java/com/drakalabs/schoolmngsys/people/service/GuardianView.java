package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Guardian;
import java.util.UUID;

public record GuardianView(UUID id, String firstName, String lastName, String phone, String email, String occupation, String address) {

    public static GuardianView from(Guardian guardian) {
        return new GuardianView(
                guardian.getId(),
                guardian.getFirstName(),
                guardian.getLastName(),
                guardian.getPhone(),
                guardian.getEmail(),
                guardian.getOccupation(),
                guardian.getAddress());
    }
}
