package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateStaffRequest(
        @NotBlank String staffNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull StaffType staffType,
        String gesRegistrationNumber,
        @NotNull LocalDate employmentStartDate) {
}
