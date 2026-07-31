package com.drakalabs.schoolmngsys.academics.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignSubjectTeacherRequest(@NotNull UUID teacherStaffId) {
}
