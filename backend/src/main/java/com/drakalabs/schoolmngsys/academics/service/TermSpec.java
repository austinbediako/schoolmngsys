package com.drakalabs.schoolmngsys.academics.service;

import java.time.LocalDate;

public record TermSpec(int termNumber, LocalDate startDate, LocalDate endDate, int expectedSchoolDays) {
}
