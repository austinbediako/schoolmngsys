package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassSubjectOfferingView;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentComponent;
import com.drakalabs.schoolmngsys.assessment.domain.Score;
import com.drakalabs.schoolmngsys.assessment.domain.TermResult;
import com.drakalabs.schoolmngsys.assessment.repository.AssessmentComponentRepository;
import com.drakalabs.schoolmngsys.assessment.repository.ScoreRepository;
import com.drakalabs.schoolmngsys.assessment.repository.TermResultRepository;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-RES-03: computes weighted totals, grades, and subject positions — server-side, idempotent,
 * all-or-nothing (BR-AA-007: a single missing score for anyone on the roster blocks the whole
 * class's submission, rather than partially computing). This is the school's trust surface
 * (CLAUDE.md testing philosophy) — every branch here is exhaustively unit-tested.
 */
@Service
public class ResultComputationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AssessmentComponentRepository assessmentComponentRepository;
    private final ScoreRepository scoreRepository;
    private final TermResultRepository termResultRepository;
    private final ClassQueryService classQueryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final GradeScaleService gradeScaleService;

    public ResultComputationService(
            AssessmentComponentRepository assessmentComponentRepository,
            ScoreRepository scoreRepository,
            TermResultRepository termResultRepository,
            ClassQueryService classQueryService,
            EnrollmentQueryService enrollmentQueryService,
            GradeScaleService gradeScaleService) {
        this.assessmentComponentRepository = assessmentComponentRepository;
        this.scoreRepository = scoreRepository;
        this.termResultRepository = termResultRepository;
        this.classQueryService = classQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.gradeScaleService = gradeScaleService;
    }

    @Audited(action = "TERM_RESULTS_COMPUTED", entityType = "TermResult")
    @Transactional
    public List<TermResultView> computeAndSubmit(UUID classSubjectOfferingId, UUID termId) {
        ClassSubjectOfferingView offering = classQueryService.getOffering(classSubjectOfferingId);

        List<AssessmentComponent> components =
                assessmentComponentRepository.findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(classSubjectOfferingId, termId);
        List<AssessmentComponent> sbaComponents =
                components.stream().filter(c -> c.getCategory() == AssessmentCategory.SBA).toList();
        List<AssessmentComponent> examComponents =
                components.stream().filter(c -> c.getCategory() == AssessmentCategory.EXAM).toList();
        requireCompleteCategory(sbaComponents, AssessmentCategory.SBA);
        requireCompleteCategory(examComponents, AssessmentCategory.EXAM);

        var gradeScale = gradeScaleService.getByYear(offering.academicYearId());

        List<EnrollmentView> roster = enrollmentQueryService.roster(offering.classId(), offering.academicYearId());

        // Pass 1: validate every enrollment has a resolution for every component (BR-AA-007) —
        // all-or-nothing, so gather every gap before rejecting rather than failing on the first.
        List<String> missing = new ArrayList<>();
        Map<UUID, Map<UUID, Score>> scoresByEnrollmentThenComponent = new java.util.HashMap<>();
        for (EnrollmentView enrollment : roster) {
            Map<UUID, Score> byComponent = new java.util.HashMap<>();
            for (AssessmentComponent component : components) {
                scoreRepository
                        .findByAssessmentComponentIdAndEnrollmentIdAndArchivedAtIsNull(component.getId(), enrollment.id())
                        .ifPresentOrElse(
                                score -> byComponent.put(component.getId(), score),
                                () -> missing.add(component.getTitle() + " for enrollment " + enrollment.id()));
            }
            scoresByEnrollmentThenComponent.put(enrollment.id(), byComponent);
        }
        if (!missing.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "BR-AA-007", "Missing score resolutions before submission: " + String.join("; ", missing));
        }

        // Pass 2: compute and persist.
        List<TermResult> results = new ArrayList<>();
        for (EnrollmentView enrollment : roster) {
            Map<UUID, Score> byComponent = scoresByEnrollmentThenComponent.get(enrollment.id());
            BigDecimal sbaTotal = categoryTotal(sbaComponents, byComponent);
            BigDecimal examTotal = categoryTotal(examComponents, byComponent);
            BigDecimal weightedTotal = sbaTotal
                    .multiply(gradeScale.sbaWeightPercent())
                    .add(examTotal.multiply(gradeScale.examWeightPercent()))
                    .divide(HUNDRED, 1, RoundingMode.HALF_UP);
            String grade = gradeScaleService.resolveGrade(offering.academicYearId(), weightedTotal);

            termResultRepository
                    .findByEnrollmentIdAndClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(
                            enrollment.id(), classSubjectOfferingId, termId)
                    .ifPresent(
                            existing -> {
                                throw new BusinessRuleViolationException(
                                        "BR-AA-003", "A term result already exists for this subject/term; use the pipeline to revise it");
                            });

            TermResult result =
                    new TermResult(enrollment.id(), classSubjectOfferingId, termId, sbaTotal, examTotal, weightedTotal, grade);
            result.submit();
            results.add(result);
        }

        assignSubjectPositions(results);
        results.forEach(termResultRepository::save);

        return results.stream().map(TermResultView::from).toList();
    }

    /**
     * Recomputes one enrollment's totals/grade from its current scores, without touching status
     * or persisting — used both by {@link #computeAndSubmit} and by the revision workflow
     * (BR-AA-006), which needs the same math applied to a single corrected student.
     */
    @Transactional(readOnly = true)
    public TermResultView recomputeOne(UUID enrollmentId, UUID classSubjectOfferingId, UUID termId) {
        ClassSubjectOfferingView offering = classQueryService.getOffering(classSubjectOfferingId);
        List<AssessmentComponent> components =
                assessmentComponentRepository.findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(classSubjectOfferingId, termId);
        List<AssessmentComponent> sbaComponents =
                components.stream().filter(c -> c.getCategory() == AssessmentCategory.SBA).toList();
        List<AssessmentComponent> examComponents =
                components.stream().filter(c -> c.getCategory() == AssessmentCategory.EXAM).toList();

        Map<UUID, Score> byComponent = new java.util.HashMap<>();
        List<String> missing = new ArrayList<>();
        for (AssessmentComponent component : components) {
            scoreRepository
                    .findByAssessmentComponentIdAndEnrollmentIdAndArchivedAtIsNull(component.getId(), enrollmentId)
                    .ifPresentOrElse(score -> byComponent.put(component.getId(), score), () -> missing.add(component.getTitle()));
        }
        if (!missing.isEmpty()) {
            throw new BusinessRuleViolationException("BR-AA-007", "Missing score resolutions: " + String.join(", ", missing));
        }

        var gradeScale = gradeScaleService.getByYear(offering.academicYearId());
        BigDecimal sbaTotal = categoryTotal(sbaComponents, byComponent);
        BigDecimal examTotal = categoryTotal(examComponents, byComponent);
        BigDecimal weightedTotal = sbaTotal
                .multiply(gradeScale.sbaWeightPercent())
                .add(examTotal.multiply(gradeScale.examWeightPercent()))
                .divide(HUNDRED, 1, RoundingMode.HALF_UP);
        String grade = gradeScaleService.resolveGrade(offering.academicYearId(), weightedTotal);

        TermResult computed = new TermResult(enrollmentId, classSubjectOfferingId, termId, sbaTotal, examTotal, weightedTotal, grade);
        return TermResultView.from(computed);
    }

    private void requireCompleteCategory(List<AssessmentComponent> components, AssessmentCategory category) {
        if (components.isEmpty()) {
            throw new BusinessRuleViolationException("BR-AA-001", "No " + category + " components defined for this subject/term");
        }
        BigDecimal totalWeight = components.stream().map(AssessmentComponent::getWeightPercent).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(HUNDRED) != 0) {
            throw new BusinessRuleViolationException(
                    "BR-AA-001", category + " component weights must sum to 100 before submission (currently " + totalWeight + ")");
        }
    }

    /**
     * A-12: a component with no real score (exempted/N-A) is excluded from both the numerator and
     * the weight denominator, and the remainder is rescaled to a 0-100 basis.
     */
    private BigDecimal categoryTotal(List<AssessmentComponent> components, Map<UUID, Score> scoresByComponent) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightUsed = BigDecimal.ZERO;

        for (AssessmentComponent component : components) {
            Score score = scoresByComponent.get(component.getId());
            if (score == null || !score.countsTowardTotal()) {
                continue;
            }
            BigDecimal percentage = score.getRawScore().divide(component.getMaxScore(), 10, RoundingMode.HALF_UP).multiply(HUNDRED);
            weightedSum = weightedSum.add(percentage.multiply(component.getWeightPercent()).divide(HUNDRED, 10, RoundingMode.HALF_UP));
            weightUsed = weightUsed.add(component.getWeightPercent());
        }

        if (weightUsed.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return weightedSum.multiply(HUNDRED).divide(weightUsed, 1, RoundingMode.HALF_UP);
    }

    /** BR-AA-004: competition ranking — equal averages share the same position (1, 2, 2, 4). */
    private void assignSubjectPositions(List<TermResult> results) {
        List<TermResult> ranked =
                results.stream().sorted(Comparator.comparing(TermResult::getWeightedTotal).reversed()).collect(Collectors.toList());

        int position = 0;
        int rank = 0;
        BigDecimal previousTotal = null;
        for (TermResult result : ranked) {
            position++;
            if (previousTotal == null || result.getWeightedTotal().compareTo(previousTotal) != 0) {
                rank = position;
            }
            result.assignSubjectPosition(rank);
            previousTotal = result.getWeightedTotal();
        }
    }
}
