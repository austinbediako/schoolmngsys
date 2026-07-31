package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.StaffStatus;
import com.drakalabs.schoolmngsys.people.domain.StaffType;
import com.drakalabs.schoolmngsys.people.service.StaffView;
import java.time.LocalDate;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        String staffNumber,
        String firstName,
        String lastName,
        StaffType staffType,
        String gesRegistrationNumber,
        LocalDate employmentStartDate,
        LocalDate employmentEndDate,
        StaffStatus status) {

    public static StaffResponse from(StaffView view) {
        return new StaffResponse(
                view.id(),
                view.staffNumber(),
                view.firstName(),
                view.lastName(),
                view.staffType(),
                view.gesRegistrationNumber(),
                view.employmentStartDate(),
                view.employmentEndDate(),
                view.status());
    }
}
