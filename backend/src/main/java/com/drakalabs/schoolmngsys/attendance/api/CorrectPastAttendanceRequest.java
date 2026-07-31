package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** BR-AT-004: a reason is mandatory for any correction to a past attendance record. */
public record CorrectPastAttendanceRequest(@NotNull AttendanceStatus status, @NotBlank String reason) {
}
