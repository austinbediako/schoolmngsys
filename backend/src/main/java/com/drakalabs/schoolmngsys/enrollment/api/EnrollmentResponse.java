package com.drakalabs.schoolmngsys.enrollment.api;

import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        UUID classId,
        UUID academicYearId,
        Integer rollNumber,
        EnrollmentStatus status,
        String exitReason,
        LocalDate exitDate) {

    public static EnrollmentResponse from(EnrollmentView view) {
        return new EnrollmentResponse(
                view.id(), view.studentId(), view.classId(), view.academicYearId(), view.rollNumber(), view.status(), view.exitReason(), view.exitDate());
    }
}
