package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.ResultComputationService;
import com.drakalabs.schoolmngsys.assessment.service.ResultPipelineService;
import com.drakalabs.schoolmngsys.assessment.service.ResultRevisionService;
import com.drakalabs.schoolmngsys.assessment.service.TermResultQueryService;
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
public class ResultController {

    private final ResultComputationService resultComputationService;
    private final ResultPipelineService resultPipelineService;
    private final ResultRevisionService resultRevisionService;
    private final TermResultQueryService termResultQueryService;

    public ResultController(
            ResultComputationService resultComputationService,
            ResultPipelineService resultPipelineService,
            ResultRevisionService resultRevisionService,
            TermResultQueryService termResultQueryService) {
        this.resultComputationService = resultComputationService;
        this.resultPipelineService = resultPipelineService;
        this.resultRevisionService = resultRevisionService;
        this.termResultQueryService = termResultQueryService;
    }

    @PostMapping("/api/v1/class-subject-offerings/{id}/results/submit")
    @PreAuthorize("hasAuthority('RESULT_SUBMIT')")
    public List<TermResultResponse> submit(@PathVariable UUID id, @RequestParam UUID termId) {
        return resultComputationService.computeAndSubmit(id, termId).stream().map(TermResultResponse::from).toList();
    }

    @PostMapping("/api/v1/class-subject-offerings/{id}/results/approve")
    @PreAuthorize("hasAuthority('RESULT_APPROVE')")
    public List<TermResultResponse> approve(@PathVariable UUID id, @RequestParam UUID termId) {
        return resultPipelineService.approveSubjectResults(id, termId).stream().map(TermResultResponse::from).toList();
    }

    @PostMapping("/api/v1/classes/{id}/results/publish")
    @PreAuthorize("hasAuthority('RESULT_PUBLISH')")
    public List<TermResultResponse> publish(@PathVariable UUID id, @RequestParam UUID academicYearId, @RequestParam UUID termId) {
        return resultPipelineService.publishClassResults(id, academicYearId, termId).stream().map(TermResultResponse::from).toList();
    }

    @PostMapping("/api/v1/term-results/{id}/revise")
    @PreAuthorize("hasAuthority('RESULT_REVISE')")
    public TermResultResponse revise(@PathVariable UUID id, @RequestBody @Valid ReviseResultRequest request) {
        return TermResultResponse.from(resultRevisionService.revise(id, request.reason()));
    }

    @GetMapping("/api/v1/enrollments/{enrollmentId}/term-results")
    @PreAuthorize("hasAuthority('RESULT_VIEW')")
    public List<TermResultResponse> currentForEnrollment(@PathVariable UUID enrollmentId, @RequestParam UUID termId) {
        return termResultQueryService.currentForEnrollment(enrollmentId, termId).stream().map(TermResultResponse::from).toList();
    }

    @GetMapping("/api/v1/enrollments/{enrollmentId}/term-results/history")
    @PreAuthorize("hasAuthority('RESULT_VIEW')")
    public List<TermResultResponse> history(@PathVariable UUID enrollmentId) {
        return termResultQueryService.fullHistory(enrollmentId).stream().map(TermResultResponse::from).toList();
    }
}
