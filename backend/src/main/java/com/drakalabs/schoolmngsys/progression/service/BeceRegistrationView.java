package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.domain.BeceRegistration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BeceRegistrationView(
        UUID id,
        UUID enrollmentId,
        UUID studentId,
        String indexNumber,
        String snapshotFirstName,
        String snapshotLastName,
        LocalDate snapshotDob,
        Instant registeredAt
) {

    public static BeceRegistrationView from(BeceRegistration reg) {
        return new BeceRegistrationView(
                reg.getId(),
                reg.getEnrollmentId(),
                reg.getStudentId(),
                reg.getIndexNumber(),
                reg.getSnapshotFirstName(),
                reg.getSnapshotLastName(),
                reg.getSnapshotDob(),
                reg.getRegisteredAt()
        );
    }
}
