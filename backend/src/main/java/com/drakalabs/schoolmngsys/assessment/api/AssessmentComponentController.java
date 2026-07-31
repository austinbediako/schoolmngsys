package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.AssessmentComponentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssessmentComponentController {

    private final AssessmentComponentService assessmentComponentService;

    public AssessmentComponentController(AssessmentComponentService assessmentComponentService) {
        this.assessmentComponentService = assessmentComponentService;
    }

    @PostMapping("/api/v1/class-subject-offerings/{id}/assessment-components")
    @PreAuthorize("hasAuthority('ASSESSMENT_COMPONENT_MANAGE')")
    public AssessmentComponentResponse create(@PathVariable UUID id, @RequestBody @Valid CreateAssessmentComponentRequest request) {
        return AssessmentComponentResponse.from(
                assessmentComponentService.createComponent(
                        id, request.termId(), request.title(), request.category(), request.maxScore(), request.weightPercent(),
                        request.assessmentDate()));
    }

    @GetMapping("/api/v1/class-subject-offerings/{id}/assessment-components")
    @PreAuthorize("hasAuthority('RESULT_VIEW')")
    public List<AssessmentComponentResponse> list(@PathVariable UUID id, @RequestParam UUID termId) {
        return assessmentComponentService.list(id, termId).stream().map(AssessmentComponentResponse::from).toList();
    }
}
