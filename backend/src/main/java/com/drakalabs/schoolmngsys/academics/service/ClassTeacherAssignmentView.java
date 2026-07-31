package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.ClassTeacherAssignment;
import java.util.UUID;

public record ClassTeacherAssignmentView(UUID id, UUID classId, UUID academicYearId, UUID teacherStaffId) {

    public static ClassTeacherAssignmentView from(ClassTeacherAssignment assignment) {
        return new ClassTeacherAssignmentView(
                assignment.getId(),
                assignment.getSchoolClass().getId(),
                assignment.getAcademicYear().getId(),
                assignment.getTeacherStaffId());
    }
}
