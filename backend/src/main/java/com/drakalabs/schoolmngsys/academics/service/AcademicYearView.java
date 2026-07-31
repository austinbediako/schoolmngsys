package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearView(UUID id, String label, LocalDate startDate, LocalDate endDate, AcademicYearStatus status) {

    public static AcademicYearView from(AcademicYear year) {
        return new AcademicYearView(year.getId(), year.getLabel(), year.getStartDate(), year.getEndDate(), year.getStatus());
    }
}
