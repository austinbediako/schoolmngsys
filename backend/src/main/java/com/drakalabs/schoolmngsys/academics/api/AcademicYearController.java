package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final AcademicYearQueryService academicYearQueryService;

    public AcademicYearController(AcademicYearService academicYearService, AcademicYearQueryService academicYearQueryService) {
        this.academicYearService = academicYearService;
        this.academicYearQueryService = academicYearQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_VIEW')")
    public PageResponse<AcademicYearResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(academicYearQueryService.list(pageable).map(AcademicYearResponse::from));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_CREATE')")
    public AcademicYearResponse create(@RequestBody @Valid CreateAcademicYearRequest request) {
        List<TermSpec> termSpecs =
                request.terms().stream()
                        .map(t -> new TermSpec(t.termNumber(), t.startDate(), t.endDate(), t.expectedSchoolDays()))
                        .toList();
        return AcademicYearResponse.from(
                academicYearService.createYear(request.label(), request.startDate(), request.endDate(), termSpecs));
    }

    @GetMapping("/{id}/terms")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_VIEW')")
    public List<TermResponse> listTerms(@PathVariable UUID id) {
        return academicYearQueryService.listTerms(id).stream().map(TermResponse::from).toList();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_UPDATE')")
    public AcademicYearResponse activate(@PathVariable UUID id) {
        return AcademicYearResponse.from(academicYearService.activateYear(id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ACADEMIC_YEAR_CLOSE')")
    public AcademicYearResponse close(@PathVariable UUID id) {
        return AcademicYearResponse.from(academicYearService.closeYear(id));
    }
}
