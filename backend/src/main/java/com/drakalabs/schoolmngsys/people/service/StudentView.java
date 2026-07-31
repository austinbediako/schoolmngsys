package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.Student;
import com.drakalabs.schoolmngsys.people.domain.StudentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record StudentView(
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

    public static StudentView from(Student student) {
        return new StudentView(
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getOtherNames(),
                student.getDateOfBirth(),
                student.getGender(),
                student.getAdmissionDate(),
                student.getStatus(),
                student.getNationality(),
                student.getPreviousSchool(),
                student.getResidentialAddress(),
                student.getEmergencyContactName(),
                student.getEmergencyContactPhone(),
                student.getEmergencyContactRelationship());
    }
}
