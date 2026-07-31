package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Staff;
import com.drakalabs.schoolmngsys.people.domain.StaffStatus;
import com.drakalabs.schoolmngsys.people.domain.StaffType;
import java.time.LocalDate;
import java.util.UUID;

public record StaffView(
        UUID id,
        String staffNumber,
        String firstName,
        String lastName,
        StaffType staffType,
        String gesRegistrationNumber,
        LocalDate employmentStartDate,
        LocalDate employmentEndDate,
        StaffStatus status) {

    public static StaffView from(Staff staff) {
        return new StaffView(
                staff.getId(),
                staff.getStaffNumber(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getStaffType(),
                staff.getGesRegistrationNumber(),
                staff.getEmploymentStartDate(),
                staff.getEmploymentEndDate(),
                staff.getStatus());
    }
}
