package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The "is date a school day for level X?" query other modules (attendance, WP-5) will call. */
@Service
public class SchoolDayQueryService {

    private final ClassLevelRepository classLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TermCalendarService termCalendarService;

    public SchoolDayQueryService(
            ClassLevelRepository classLevelRepository,
            AcademicYearRepository academicYearRepository,
            TermCalendarService termCalendarService) {
        this.classLevelRepository = classLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.termCalendarService = termCalendarService;
    }

    @Transactional(readOnly = true)
    public boolean isSchoolDay(LocalDate date, String classLevelCode, UUID academicYearId) {
        ClassLevel level = classLevelRepository
                .findByCodeAndArchivedAtIsNull(classLevelCode)
                .orElseThrow(() -> new NotFoundException("No such class level: " + classLevelCode));
        AcademicYear academicYear = academicYearRepository
                .findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("No such academic year: " + academicYearId));
        return termCalendarService.isSchoolDay(date, level, academicYear);
    }
}
