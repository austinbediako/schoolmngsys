package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.TermView;
import java.time.LocalDate;
import java.util.UUID;

public record TermResponse(UUID id, int termNumber, LocalDate officialStartDate, LocalDate officialEndDate, int expectedSchoolDays) {

    public static TermResponse from(TermView view) {
        return new TermResponse(
                view.id(), view.termNumber(), view.officialStartDate(), view.officialEndDate(), view.expectedSchoolDays());
    }
}
