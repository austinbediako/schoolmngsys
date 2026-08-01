package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateStudentRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String otherNames,
        @NotNull LocalDate dateOfBirth,
        @NotNull Gender gender,
        @NotNull LocalDate admissionDate,
        @NotEmpty List<@Valid GuardianLinkRequest> guardianLinks,
        String nationality,
        String previousSchool,
        String residentialAddress,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship) {

    public record GuardianLinkRequest(
            @NotNull UUID guardianId,
            @NotNull RelationshipType relationshipType,
            boolean primaryContact,
            boolean hasCustody,
            boolean receivesBilling,
            boolean receivesAcademicReports) {
    }
}
