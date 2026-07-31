package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.StudentGuardianView;
import java.util.UUID;

public record StudentGuardianResponse(
        UUID id,
        UUID studentId,
        UUID guardianId,
        RelationshipType relationshipType,
        boolean primaryContact,
        boolean hasCustody,
        boolean receivesBilling,
        boolean receivesAcademicReports) {

    public static StudentGuardianResponse from(StudentGuardianView view) {
        return new StudentGuardianResponse(
                view.id(),
                view.studentId(),
                view.guardianId(),
                view.relationshipType(),
                view.primaryContact(),
                view.hasCustody(),
                view.receivesBilling(),
                view.receivesAcademicReports());
    }
}
