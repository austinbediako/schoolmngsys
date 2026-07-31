package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentComponent;
import com.drakalabs.schoolmngsys.assessment.repository.AssessmentComponentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-AA-001: a component's weight is its share *within* its category — SBA weights (and EXAM weights) must sum to 100. */
@Service
public class AssessmentComponentService {

    private final AssessmentComponentRepository assessmentComponentRepository;

    public AssessmentComponentService(AssessmentComponentRepository assessmentComponentRepository) {
        this.assessmentComponentRepository = assessmentComponentRepository;
    }

    @Audited(action = "ASSESSMENT_COMPONENT_CREATED", entityType = "AssessmentComponent")
    @Transactional
    public AssessmentComponentView createComponent(
            UUID classSubjectOfferingId,
            UUID termId,
            String title,
            AssessmentCategory category,
            BigDecimal maxScore,
            BigDecimal weightPercent,
            LocalDate assessmentDate) {
        BigDecimal existingWeight =
                assessmentComponentRepository
                        .findByClassSubjectOfferingIdAndTermIdAndCategoryAndArchivedAtIsNull(classSubjectOfferingId, termId, category)
                        .stream()
                        .map(AssessmentComponent::getWeightPercent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (existingWeight.add(weightPercent).compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessRuleViolationException(
                    "BR-AA-001", category + " component weights for this subject/term cannot exceed 100%");
        }

        return AssessmentComponentView.from(
                assessmentComponentRepository.save(
                        new AssessmentComponent(classSubjectOfferingId, termId, title, category, maxScore, weightPercent, assessmentDate)));
    }

    @Transactional(readOnly = true)
    public List<AssessmentComponentView> list(UUID classSubjectOfferingId, UUID termId) {
        return assessmentComponentRepository
                .findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(classSubjectOfferingId, termId)
                .stream()
                .map(AssessmentComponentView::from)
                .toList();
    }
}
