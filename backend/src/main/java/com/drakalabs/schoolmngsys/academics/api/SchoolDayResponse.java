package com.drakalabs.schoolmngsys.academics.api;

import java.time.LocalDate;

public record SchoolDayResponse(LocalDate date, boolean schoolDay) {
}
