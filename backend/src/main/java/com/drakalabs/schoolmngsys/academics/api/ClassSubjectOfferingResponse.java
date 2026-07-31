package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.ClassSubjectOfferingView;
import java.util.UUID;

public record ClassSubjectOfferingResponse(UUID id, UUID classId, UUID subjectId, UUID academicYearId, UUID teacherStaffId) {

    public static ClassSubjectOfferingResponse from(ClassSubjectOfferingView view) {
        return new ClassSubjectOfferingResponse(
                view.id(), view.classId(), view.subjectId(), view.academicYearId(), view.teacherStaffId());
    }
}
