package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import java.util.UUID;

public record GuardianLinkSpec(
        UUID guardianId,
        RelationshipType relationshipType,
        boolean primaryContact,
        boolean hasCustody,
        boolean receivesBilling,
        boolean receivesAcademicReports) {
}
