package com.drakalabs.schoolmngsys.academics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import com.drakalabs.schoolmngsys.academics.domain.SchoolDayException;
import com.drakalabs.schoolmngsys.academics.domain.SchoolDayExceptionType;
import com.drakalabs.schoolmngsys.academics.domain.Term;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.repository.SchoolDayExceptionRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermCalendarVariantRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-2 test plan (docs/14 §5): "calendar-variant resolution (JHS 3 dates differ)" and
 * "school-day computation across holidays" (BR-AS-003).
 */
class TermCalendarServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private TermCalendarVariantRepository termCalendarVariantRepository;

    @Autowired
    private SchoolDayExceptionRepository schoolDayExceptionRepository;

    @Autowired
    private TermCalendarService termCalendarService;

    @Autowired
    private com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository academicYearRepository;

    private List<TermSpec> threeTerms() {
        return List.of(
                new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65));
    }

    @Test
    void jhs3HasEarlierTermDatesThanOtherLevelsWhenAVariantExists() {
        AcademicYearView year = academicYearService.createYear(
                "Y-" + UUID.randomUUID(), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 1), threeTerms());
        Term term2 = termRepository.findByAcademicYearIdAndTermNumberAndArchivedAtIsNull(year.id(), 2).orElseThrow();
        ClassLevel jhs3 = classLevelRepository.findByCodeAndArchivedAtIsNull("B9").orElseThrow();
        ClassLevel primary1 = classLevelRepository.findByCodeAndArchivedAtIsNull("B1").orElseThrow();

        LocalDate jhs3EarlierEnd = LocalDate.of(2027, 3, 20);
        termCalendarVariantRepository.save(
                new com.drakalabs.schoolmngsys.academics.domain.TermCalendarVariant(
                        term2, jhs3, term2.getOfficialStartDate(), jhs3EarlierEnd));

        DateRange jhs3Range = termCalendarService.resolveTermDateRange(term2, jhs3);
        DateRange primary1Range = termCalendarService.resolveTermDateRange(term2, primary1);

        assertThat(jhs3Range.end()).isEqualTo(jhs3EarlierEnd);
        assertThat(primary1Range.end()).isEqualTo(term2.getOfficialEndDate());
        assertThat(jhs3Range.end()).isBefore(primary1Range.end());
    }

    @Test
    void schoolDayComputationHonoursWeekendsHolidaysAndTermBoundaries() {
        AcademicYearView year = academicYearService.createYear(
                "Y-" + UUID.randomUUID(), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 1), threeTerms());
        var academicYear = academicYearRepository.findById(year.id()).orElseThrow();
        ClassLevel primary1 = classLevelRepository.findByCodeAndArchivedAtIsNull("B1").orElseThrow();

        LocalDate ordinaryWeekday = LocalDate.of(2026, 9, 2); // Wednesday, within Term 1
        LocalDate saturday = LocalDate.of(2026, 9, 5);
        LocalDate holiday = LocalDate.of(2026, 9, 3); // Thursday, marked as a holiday
        LocalDate outsideAnyTerm = LocalDate.of(2026, 8, 15); // before the year starts

        schoolDayExceptionRepository.save(new SchoolDayException(holiday, SchoolDayExceptionType.HOLIDAY, "Founders' Day"));

        assertThat(termCalendarService.isSchoolDay(ordinaryWeekday, primary1, academicYear)).isTrue();
        assertThat(termCalendarService.isSchoolDay(saturday, primary1, academicYear)).isFalse();
        assertThat(termCalendarService.isSchoolDay(holiday, primary1, academicYear)).isFalse();
        assertThat(termCalendarService.isSchoolDay(outsideAnyTerm, primary1, academicYear)).isFalse();
    }
}
