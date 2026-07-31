package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.GradeScaleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GradeScaleController {

    private final GradeScaleService gradeScaleService;

    public GradeScaleController(GradeScaleService gradeScaleService) {
        this.gradeScaleService = gradeScaleService;
    }

    @PostMapping("/api/v1/academic-years/{id}/grade-scale/default")
    @PreAuthorize("hasAuthority('GRADE_SCALE_MANAGE')")
    public GradeScaleResponse createDefault(@PathVariable UUID id) {
        return GradeScaleResponse.from(gradeScaleService.createDefault(id));
    }

    @PostMapping("/api/v1/academic-years/{id}/grade-scale")
    @PreAuthorize("hasAuthority('GRADE_SCALE_MANAGE')")
    public GradeScaleResponse createCustom(@PathVariable UUID id, @RequestBody @Valid CreateCustomGradeScaleRequest request) {
        List<GradeScaleService.BandSpec> bands =
                request.bands().stream()
                        .map(b -> new GradeScaleService.BandSpec(b.minScore(), b.maxScore(), b.grade(), b.description()))
                        .toList();
        return GradeScaleResponse.from(
                gradeScaleService.createCustom(id, request.sbaWeightPercent(), request.examWeightPercent(), bands));
    }

    @GetMapping("/api/v1/academic-years/{id}/grade-scale")
    @PreAuthorize("hasAuthority('RESULT_VIEW')")
    public GradeScaleResponse get(@PathVariable UUID id) {
        return GradeScaleResponse.from(gradeScaleService.getByYear(id));
    }
}
