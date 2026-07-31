package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.StudentStatus;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        String studentNumber,
        String firstName,
        String lastName,
        String otherNames,
        LocalDate dateOfBirth,
        Gender gender,
        LocalDate admissionDate,
        StudentStatus status,
        String nationality,
        String previousSchool,
        String residentialAddress,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship) {

    public static StudentResponse from(StudentView view) {
        return new StudentResponse(
                view.id(),
                view.studentNumber(),
                view.firstName(),
                view.lastName(),
                view.otherNames(),
                view.dateOfBirth(),
                view.gender(),
                view.admissionDate(),
                view.status(),
                view.nationality(),
                view.previousSchool(),
                view.residentialAddress(),
                view.emergencyContactName(),
                view.emergencyContactPhone(),
                view.emergencyContactRelationship());
    }
}
