package com.drakalabs.schoolmngsys.academics.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreateAcademicYearRequest(
        @NotBlank String label,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @Size(min = 3, max = 3) List<@Valid TermRequest> terms) {

    public record TermRequest(
            int termNumber, @NotNull LocalDate startDate, @NotNull LocalDate endDate, int expectedSchoolDays) {
    }
}
