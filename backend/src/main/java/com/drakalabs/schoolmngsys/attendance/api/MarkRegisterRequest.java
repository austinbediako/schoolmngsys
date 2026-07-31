package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MarkRegisterRequest(
        @NotNull UUID classId, @NotNull UUID academicYearId, @NotNull LocalDate date, @NotEmpty List<@Valid Entry> entries) {

    public record Entry(@NotNull UUID enrollmentId, @NotNull AttendanceStatus status) {
    }
}
