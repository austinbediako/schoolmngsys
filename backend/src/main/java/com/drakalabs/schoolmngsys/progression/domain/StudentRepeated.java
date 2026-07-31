package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.util.UUID;

public class StudentRepeated extends DomainEvent {

    private final UUID studentId;
    private final UUID classId;
    private final UUID targetAcademicYearId;
    private final String justification;

    public StudentRepeated(UUID studentId, UUID classId, UUID targetAcademicYearId, String justification) {
        this.studentId = studentId;
        this.classId = classId;
        this.targetAcademicYearId = targetAcademicYearId;
        this.justification = justification;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getClassId() {
        return classId;
    }

    public UUID getTargetAcademicYearId() {
        return targetAcademicYearId;
    }

    public String getJustification() {
        return justification;
    }
}
