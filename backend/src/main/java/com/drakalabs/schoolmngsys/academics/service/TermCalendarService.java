package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import com.drakalabs.schoolmngsys.academics.domain.Term;
import com.drakalabs.schoolmngsys.academics.repository.SchoolDayExceptionRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermCalendarVariantRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves term dates per class level (BR-AS-003: JHS 3's Terms 2-3 end earlier) and computes
 * whether a given date is a school day for a level (FR-ACAD-02) — weekends and
 * {@code school_day_exceptions} are never school days regardless of term dates.
 */
@Service
public class TermCalendarService {

    private final TermRepository termRepository;
    private final TermCalendarVariantRepository termCalendarVariantRepository;
    private final SchoolDayExceptionRepository schoolDayExceptionRepository;

    public TermCalendarService(
            TermRepository termRepository,
            TermCalendarVariantRepository termCalendarVariantRepository,
            SchoolDayExceptionRepository schoolDayExceptionRepository) {
        this.termRepository = termRepository;
        this.termCalendarVariantRepository = termCalendarVariantRepository;
        this.schoolDayExceptionRepository = schoolDayExceptionRepository;
    }

    @Transactional(readOnly = true)
    public DateRange resolveTermDateRange(Term term, ClassLevel classLevel) {
        return termCalendarVariantRepository
                .findByTermIdAndClassLevelIdAndArchivedAtIsNull(term.getId(), classLevel.getId())
                .map(variant -> new DateRange(variant.getOverrideStartDate(), variant.getOverrideEndDate()))
                .orElseGet(() -> new DateRange(term.getOfficialStartDate(), term.getOfficialEndDate()));
    }

    @Transactional(readOnly = true)
    public boolean isSchoolDay(LocalDate date, ClassLevel classLevel, AcademicYear academicYear) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }
        if (schoolDayExceptionRepository.findByExceptionDateAndArchivedAtIsNull(date).isPresent()) {
            return false;
        }

        return termRepository.findByAcademicYearIdAndArchivedAtIsNullOrderByTermNumber(academicYear.getId()).stream()
                .anyMatch(term -> resolveTermDateRange(term, classLevel).contains(date));
    }
}
