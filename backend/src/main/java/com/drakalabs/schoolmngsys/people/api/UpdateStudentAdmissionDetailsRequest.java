package com.drakalabs.schoolmngsys.people.api;

public record UpdateStudentAdmissionDetailsRequest(
        String nationality,
        String previousSchool,
        String residentialAddress,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship) {
}
