package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.SchoolDayQueryService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school-days")
public class SchoolDayController {

    private final SchoolDayQueryService schoolDayQueryService;

    public SchoolDayController(SchoolDayQueryService schoolDayQueryService) {
        this.schoolDayQueryService = schoolDayQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_VIEW')")
    public SchoolDayResponse isSchoolDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String classLevelCode,
            @RequestParam UUID academicYearId) {
        return new SchoolDayResponse(date, schoolDayQueryService.isSchoolDay(date, classLevelCode, academicYearId));
    }
}
