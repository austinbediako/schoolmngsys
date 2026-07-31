package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.service.BeceQueryService;
import com.drakalabs.schoolmngsys.progression.service.BeceRegistrationView;
import com.drakalabs.schoolmngsys.progression.service.BeceResultView;
import com.drakalabs.schoolmngsys.progression.service.BeceService;
import com.drakalabs.schoolmngsys.progression.service.BeceSubjectScoreSpec;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bece")
public class BeceController {

    private final BeceService beceService;
    private final BeceQueryService beceQueryService;

    public BeceController(BeceService beceService, BeceQueryService beceQueryService) {
        this.beceService = beceService;
        this.beceQueryService = beceQueryService;
    }

    @PostMapping("/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('BECE_REGISTER')")
    public BeceRegistrationResponse registerCandidate(@Valid @RequestBody RegisterBeceCandidateRequest request) {
        BeceRegistrationView view = beceService.registerCandidate(request.enrollmentId(), request.indexNumber());
        return BeceRegistrationResponse.from(view);
    }

    @GetMapping("/registrations/{id}")
    @PreAuthorize("hasAnyAuthority('BECE_REGISTER', 'BECE_SCORE_ENTER')")
    public BeceRegistrationResponse getRegistration(@PathVariable UUID id) {
        BeceRegistrationView view = beceQueryService.getRegistration(id);
        return BeceRegistrationResponse.from(view);
    }

    @GetMapping("/registrations")
    @PreAuthorize("hasAnyAuthority('BECE_REGISTER', 'BECE_SCORE_ENTER')")
    public BeceRegistrationResponse getRegistrationByEnrollment(@RequestParam UUID enrollmentId) {
        BeceRegistrationView view = beceQueryService.getRegistrationByEnrollment(enrollmentId);
        return BeceRegistrationResponse.from(view);
    }

    @PostMapping("/registrations/{id}/results")
    @PreAuthorize("hasAuthority('BECE_SCORE_ENTER')")
    public List<BeceResultResponse> importResults(
            @PathVariable UUID id,
            @Valid @RequestBody ImportBeceResultsRequest request) {
        List<BeceSubjectScoreSpec> specs = request.scores().stream()
                .map(s -> new BeceSubjectScoreSpec(s.subjectId(), s.grade()))
                .toList();

        List<BeceResultView> views = beceService.importResults(id, specs);
        return views.stream().map(BeceResultResponse::from).toList();
    }

    @GetMapping("/registrations/{id}/results")
    @PreAuthorize("hasAnyAuthority('BECE_REGISTER', 'BECE_SCORE_ENTER')")
    public List<BeceResultResponse> listResults(@PathVariable UUID id) {
        List<BeceResultView> views = beceQueryService.listResults(id);
        return views.stream().map(BeceResultResponse::from).toList();
    }
}
