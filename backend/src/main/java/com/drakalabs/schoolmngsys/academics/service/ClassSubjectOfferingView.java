package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.ClassSubjectOffering;
import java.util.UUID;

public record ClassSubjectOfferingView(UUID id, UUID classId, UUID subjectId, UUID academicYearId, UUID teacherStaffId) {

    public static ClassSubjectOfferingView from(ClassSubjectOffering offering) {
        return new ClassSubjectOfferingView(
                offering.getId(),
                offering.getSchoolClass().getId(),
                offering.getSubject().getId(),
                offering.getAcademicYear().getId(),
                offering.getTeacherStaffId());
    }
}
