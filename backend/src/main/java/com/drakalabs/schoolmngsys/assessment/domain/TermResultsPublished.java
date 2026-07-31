package com.drakalabs.schoolmngsys.assessment.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.util.List;
import java.util.UUID;

/** FR-RES-06/docs/02 §4: consumed by communication (guardian SMS) and the parent portal once built (WP-8). */
public class TermResultsPublished extends DomainEvent {

    private final UUID classId;
    private final UUID academicYearId;
    private final UUID termId;
    private final List<UUID> enrollmentIds;

    public TermResultsPublished(UUID classId, UUID academicYearId, UUID termId, List<UUID> enrollmentIds) {
        this.classId = classId;
        this.academicYearId = academicYearId;
        this.termId = termId;
        this.enrollmentIds = List.copyOf(enrollmentIds);
    }

    public UUID getClassId() {
        return classId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public UUID getTermId() {
        return termId;
    }

    public List<UUID> getEnrollmentIds() {
        return enrollmentIds;
    }
}
