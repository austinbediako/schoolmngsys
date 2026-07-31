package com.drakalabs.schoolmngsys.people.service;

/**
 * Optional admission-record fields (FR-STU-01 extension, WP-11) — all nullable, set as a whole via
 * {@code Student.updateAdmissionDetails}. Deliberately excludes medical information: health data is
 * ring-fenced to the (post-MVP) health module per BR-HE-001, never stored on the core Student record.
 */
public record StudentAdmissionDetails(
        String nationality,
        String previousSchool,
        String residentialAddress,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship) {
}
