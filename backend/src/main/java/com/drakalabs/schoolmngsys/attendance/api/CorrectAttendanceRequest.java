package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record CorrectAttendanceRequest(@NotNull AttendanceStatus status) {
}
