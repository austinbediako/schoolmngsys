package com.drakalabs.schoolmngsys.academics.service;

import java.time.LocalDate;

public record DateRange(LocalDate start, LocalDate end) {

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
