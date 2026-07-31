package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkGuardianRequest(
        @NotNull UUID guardianId,
        @NotNull RelationshipType relationshipType,
        boolean primaryContact,
        boolean hasCustody,
        boolean receivesBilling,
        boolean receivesAcademicReports) {
}
