package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.repository.PromotionDecisionRepository;
import com.drakalabs.schoolmngsys.progression.repository.PromotionRunRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressionQueryService {

    private final PromotionRunRepository promotionRunRepository;
    private final PromotionDecisionRepository promotionDecisionRepository;

    public ProgressionQueryService(
            PromotionRunRepository promotionRunRepository,
            PromotionDecisionRepository promotionDecisionRepository) {
        this.promotionRunRepository = promotionRunRepository;
        this.promotionDecisionRepository = promotionDecisionRepository;
    }

    @Transactional(readOnly = true)
    public PromotionRunView getRun(UUID runId) {
        return promotionRunRepository.findById(runId)
                .map(PromotionRunView::from)
                .orElseThrow(() -> new NotFoundException("No such promotion run: " + runId));
    }

    @Transactional(readOnly = true)
    public PromotionRunView getRunForSourceYear(UUID sourceAcademicYearId) {
        return promotionRunRepository.findBySourceAcademicYearIdAndArchivedAtIsNull(sourceAcademicYearId)
                .map(PromotionRunView::from)
                .orElseThrow(() -> new NotFoundException("No promotion run found for source academic year: " + sourceAcademicYearId));
    }

    @Transactional(readOnly = true)
    public List<PromotionDecisionView> listDecisions(UUID runId) {
        return promotionDecisionRepository.findByPromotionRunIdAndArchivedAtIsNull(runId).stream()
                .map(PromotionDecisionView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionDecisionView getDecision(UUID decisionId) {
        return promotionDecisionRepository.findById(decisionId)
                .map(PromotionDecisionView::from)
                .orElseThrow(() -> new NotFoundException("No such promotion decision: " + decisionId));
    }
}
