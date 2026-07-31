package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.event.DomainEvent;
import java.util.UUID;

public class StudentGraduated extends DomainEvent {

    private final UUID studentId;
    private final UUID sourceAcademicYearId;

    public StudentGraduated(UUID studentId, UUID sourceAcademicYearId) {
        this.studentId = studentId;
        this.sourceAcademicYearId = sourceAcademicYearId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getSourceAcademicYearId() {
        return sourceAcademicYearId;
    }
}
