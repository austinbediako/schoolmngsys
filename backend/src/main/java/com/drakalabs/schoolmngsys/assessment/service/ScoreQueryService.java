package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.repository.ScoreRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreQueryService {

    private final ScoreRepository scoreRepository;

    public ScoreQueryService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @Transactional(readOnly = true)
    public List<ScoreView> listForComponent(UUID assessmentComponentId, List<UUID> enrollmentIds) {
        return enrollmentIds.stream()
                .flatMap(
                        enrollmentId -> scoreRepository
                                .findByAssessmentComponentIdAndEnrollmentIdAndArchivedAtIsNull(assessmentComponentId, enrollmentId)
                                .stream())
                .map(ScoreView::from)
                .toList();
    }
}
