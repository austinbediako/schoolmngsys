package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.service.BeceRegistrationView;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BeceRegistrationResponse(
        UUID id,
        UUID enrollmentId,
        UUID studentId,
        String indexNumber,
        String snapshotFirstName,
        String snapshotLastName,
        LocalDate snapshotDob,
        Instant registeredAt
) {

    public static BeceRegistrationResponse from(BeceRegistrationView view) {
        return new BeceRegistrationResponse(
                view.id(),
                view.enrollmentId(),
                view.studentId(),
                view.indexNumber(),
                view.snapshotFirstName(),
                view.snapshotLastName(),
                view.snapshotDob(),
                view.registeredAt()
        );
    }
}
