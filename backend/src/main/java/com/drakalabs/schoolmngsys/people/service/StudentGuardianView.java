package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.domain.StudentGuardian;
import java.util.UUID;

public record StudentGuardianView(
        UUID id,
        UUID studentId,
        UUID guardianId,
        RelationshipType relationshipType,
        boolean primaryContact,
        boolean hasCustody,
        boolean receivesBilling,
        boolean receivesAcademicReports) {

    public static StudentGuardianView from(StudentGuardian link) {
        return new StudentGuardianView(
                link.getId(),
                link.getStudent().getId(),
                link.getGuardian().getId(),
                link.getRelationshipType(),
                link.isPrimaryContact(),
                link.isHasCustody(),
                link.isReceivesBilling(),
                link.isReceivesAcademicReports());
    }
}
