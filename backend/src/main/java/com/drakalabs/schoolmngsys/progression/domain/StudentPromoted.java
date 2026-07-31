package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.util.UUID;

public class StudentPromoted extends DomainEvent {

    private final UUID studentId;
    private final UUID sourceClassId;
    private final UUID targetClassId;
    private final UUID targetAcademicYearId;

    public StudentPromoted(UUID studentId, UUID sourceClassId, UUID targetClassId, UUID targetAcademicYearId) {
        this.studentId = studentId;
        this.sourceClassId = sourceClassId;
        this.targetClassId = targetClassId;
        this.targetAcademicYearId = targetAcademicYearId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getSourceClassId() {
        return sourceClassId;
    }

    public UUID getTargetClassId() {
        return targetClassId;
    }

    public UUID getTargetAcademicYearId() {
        return targetAcademicYearId;
    }
}
