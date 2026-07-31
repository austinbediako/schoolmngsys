package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.ClassTeacherAssignmentView;
import java.util.UUID;

public record ClassTeacherAssignmentResponse(UUID id, UUID classId, UUID academicYearId, UUID teacherStaffId) {

    public static ClassTeacherAssignmentResponse from(ClassTeacherAssignmentView view) {
        return new ClassTeacherAssignmentResponse(view.id(), view.classId(), view.academicYearId(), view.teacherStaffId());
    }
}
