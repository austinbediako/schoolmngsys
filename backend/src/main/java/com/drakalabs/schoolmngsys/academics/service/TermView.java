package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.Term;
import java.time.LocalDate;
import java.util.UUID;

public record TermView(
        UUID id, UUID academicYearId, int termNumber, LocalDate officialStartDate, LocalDate officialEndDate, int expectedSchoolDays) {

    public static TermView from(Term term) {
        return new TermView(
                term.getId(),
                term.getAcademicYear().getId(),
                term.getTermNumber(),
                term.getOfficialStartDate(),
                term.getOfficialEndDate(),
                term.getExpectedSchoolDays());
    }
}
