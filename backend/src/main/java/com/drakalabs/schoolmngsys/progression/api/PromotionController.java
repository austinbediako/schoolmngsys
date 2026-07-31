package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.service.ProgressionQueryService;
import com.drakalabs.schoolmngsys.progression.service.PromotionDecisionView;
import com.drakalabs.schoolmngsys.progression.service.PromotionRunView;
import com.drakalabs.schoolmngsys.progression.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final ProgressionQueryService progressionQueryService;

    public PromotionController(
            PromotionService promotionService,
            ProgressionQueryService progressionQueryService) {
        this.promotionService = promotionService;
        this.progressionQueryService = progressionQueryService;
    }

    @PostMapping("/runs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PROMOTION_PROPOSE')")
    public PromotionRunResponse initiateRun(@Valid @RequestBody InitiatePromotionRunRequest request) {
        PromotionRunView view = promotionService.initiateRun(request.sourceAcademicYearId(), request.targetAcademicYearId());
        return PromotionRunResponse.from(view);
    }

    @GetMapping("/runs/{runId}")
    @PreAuthorize("hasAnyAuthority('PROMOTION_PROPOSE', 'PROMOTION_APPROVE', 'PROMOTION_RUN_EXECUTE')")
    public PromotionRunResponse getRun(@PathVariable UUID runId) {
        PromotionRunView view = progressionQueryService.getRun(runId);
        return PromotionRunResponse.from(view);
    }

    @GetMapping("/runs/{runId}/decisions")
    @PreAuthorize("hasAnyAuthority('PROMOTION_PROPOSE', 'PROMOTION_APPROVE', 'PROMOTION_RUN_EXECUTE')")
    public List<PromotionDecisionResponse> listDecisions(@PathVariable UUID runId) {
        return progressionQueryService.listDecisions(runId).stream()
                .map(PromotionDecisionResponse::from)
                .toList();
    }

    @PutMapping("/decisions/{decisionId}")
    @PreAuthorize("hasAuthority('PROMOTION_PROPOSE')")
    public PromotionDecisionResponse proposeException(
            @PathVariable UUID decisionId,
            @Valid @RequestBody ProposeDecisionExceptionRequest request) {
        PromotionDecisionView view = promotionService.proposeException(
                decisionId, request.decisionType(), request.targetClassLevelId(), request.justification());
        return PromotionDecisionResponse.from(view);
    }

    @PutMapping("/decisions/{decisionId}/target-class")
    @PreAuthorize("hasAuthority('PROMOTION_PROPOSE')")
    public PromotionDecisionResponse assignTargetClass(
            @PathVariable UUID decisionId,
            @Valid @RequestBody AssignTargetClassRequest request) {
        PromotionDecisionView view = promotionService.assignTargetClass(decisionId, request.targetClassId());
        return PromotionDecisionResponse.from(view);
    }

    @PostMapping("/runs/{runId}/approve")
    @PreAuthorize("hasAuthority('PROMOTION_APPROVE')")
    public PromotionRunResponse approveRun(@PathVariable UUID runId) {
        PromotionRunView view = promotionService.approveRun(runId);
        return PromotionRunResponse.from(view);
    }

    @PostMapping("/runs/{runId}/execute")
    @PreAuthorize("hasAuthority('PROMOTION_RUN_EXECUTE')")
    public PromotionRunResponse executeRun(@PathVariable UUID runId) {
        PromotionRunView view = promotionService.executeRun(runId);
        return PromotionRunResponse.from(view);
    }
}
