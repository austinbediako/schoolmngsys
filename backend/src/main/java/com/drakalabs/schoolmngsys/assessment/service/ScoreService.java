package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentComponent;
import com.drakalabs.schoolmngsys.assessment.domain.Score;
import com.drakalabs.schoolmngsys.assessment.repository.AssessmentComponentRepository;
import com.drakalabs.schoolmngsys.assessment.repository.ScoreRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-AA-002 (bounds) / BR-AA-007 (missing-score flags, never defaulted to zero). */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final AssessmentComponentRepository assessmentComponentRepository;

    public ScoreService(ScoreRepository scoreRepository, AssessmentComponentRepository assessmentComponentRepository) {
        this.scoreRepository = scoreRepository;
        this.assessmentComponentRepository = assessmentComponentRepository;
    }

    @Audited(action = "SCORES_ENTERED", entityType = "Score")
    @Transactional
    public List<ScoreView> enterScoresBulk(UUID assessmentComponentId, List<ScoreEntry> entries) {
        AssessmentComponent component = assessmentComponentRepository
                .findById(assessmentComponentId)
                .orElseThrow(() -> new NotFoundException("No such assessment component: " + assessmentComponentId));

        return entries.stream().map(entry -> saveEntry(component, entry)).toList();
    }

    private ScoreView saveEntry(AssessmentComponent component, ScoreEntry entry) {
        int resolutionCount =
                (entry.rawScore() != null ? 1 : 0) + (entry.exempted() ? 1 : 0) + (entry.naReason() != null && !entry.naReason().isBlank() ? 1 : 0);
        if (resolutionCount != 1) {
            throw new BusinessRuleViolationException(
                    "BR-AA-007", "Exactly one of a score, an exemption, or an N/A reason must be provided");
        }

        if (entry.rawScore() != null) {
            if (entry.rawScore().compareTo(BigDecimal.ZERO) < 0 || entry.rawScore().compareTo(component.getMaxScore()) > 0) {
                throw new BusinessRuleViolationException(
                        "BR-AA-002", "Raw score must be between 0 and " + component.getMaxScore());
            }
        }

        Score score = scoreRepository
                .findByAssessmentComponentIdAndEnrollmentIdAndArchivedAtIsNull(component.getId(), entry.enrollmentId())
                .orElse(null);

        Score toSave = entry.rawScore() != null
                ? Score.scored(component.getId(), entry.enrollmentId(), entry.rawScore())
                : entry.exempted()
                        ? Score.exempted(component.getId(), entry.enrollmentId())
                        : Score.notApplicable(component.getId(), entry.enrollmentId(), entry.naReason());

        if (score != null) {
            score.archive();
            scoreRepository.save(score);
        }

        return ScoreView.from(scoreRepository.save(toSave));
    }
}
