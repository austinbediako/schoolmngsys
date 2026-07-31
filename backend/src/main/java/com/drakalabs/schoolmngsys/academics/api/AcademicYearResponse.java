package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearResponse(UUID id, String label, LocalDate startDate, LocalDate endDate, AcademicYearStatus status) {

    public static AcademicYearResponse from(AcademicYearView view) {
        return new AcademicYearResponse(view.id(), view.label(), view.startDate(), view.endDate(), view.status());
    }
}
