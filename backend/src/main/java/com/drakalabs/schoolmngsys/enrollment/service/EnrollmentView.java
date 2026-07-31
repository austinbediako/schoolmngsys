package com.drakalabs.schoolmngsys.enrollment.service;

import com.drakalabs.schoolmngsys.enrollment.domain.Enrollment;
import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentView(
        UUID id,
        UUID studentId,
        UUID classId,
        UUID academicYearId,
        Integer rollNumber,
        EnrollmentStatus status,
        String exitReason,
        LocalDate exitDate) {

    public static EnrollmentView from(Enrollment enrollment) {
        return new EnrollmentView(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getClassId(),
                enrollment.getAcademicYearId(),
                enrollment.getRollNumber(),
                enrollment.getStatus(),
                enrollment.getExitReason(),
                enrollment.getExitDate());
    }
}
