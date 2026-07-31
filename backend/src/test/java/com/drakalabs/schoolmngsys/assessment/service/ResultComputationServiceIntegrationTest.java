package com.drakalabs.schoolmngsys.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.repository.SubjectRepository;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.SubjectOfferingService;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-6 (docs/14 §5, "highest-value suite"): weighted-total math, BR-AA-007 missing-score
 * all-or-nothing block, BR-AA-001 weight-sum block, A-12 exempted/N-A renormalization, and
 * BR-AA-004 competition-ranking ties.
 */
class ResultComputationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResultComputationService resultComputationService;

    @Autowired
    private AssessmentComponentService assessmentComponentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private GradeScaleService gradeScaleService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private AcademicYearQueryService academicYearQueryService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SubjectOfferingService subjectOfferingService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private SubjectRepository subjectRepository;

    private int phoneCounter = 5000000;

    private AcademicYearView newYearWithScale() {
        AcademicYearView year = academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
        gradeScaleService.createDefault(year.id());
        return year;
    }

    private UUID firstTermId(AcademicYearView year) {
        return academicYearQueryService.listTerms(year.id()).get(0).id();
    }

    private UUID mathsSubjectId() {
        return subjectRepository.findAll().stream().filter(s -> s.getCode().equals("MATH")).findFirst().orElseThrow().getId();
    }

    private UUID newEnrollment(AcademicYearView year, UUID classId) {
        GuardianView guardian = guardianService.createGuardian("Ama", "Tetteh", "+23321" + (phoneCounter++), null, null, null);
        StudentView student = studentService.createStudent(
                "Kojo",
                "Tetteh",
                null,
                LocalDate.of(2016, 6, 1),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.FATHER, true, true, true, true)));
        return enrollmentService.enroll(student.id(), classId, year.id(), 1).id();
    }

    @Test
    void weightedTotalCombinesSbaAndExamPerTheYearsGradeScale() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B4", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        var sba = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());

        scoreService.enterScoresBulk(sba.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("90"))));
        scoreService.enterScoresBulk(exam.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("90"))));

        List<TermResultView> results = resultComputationService.computeAndSubmit(offeringId, termId);

        assertThat(results).hasSize(1);
        TermResultView result = results.get(0);
        assertThat(result.sbaTotal()).isEqualByComparingTo("90.0");
        assertThat(result.examTotal()).isEqualByComparingTo("90.0");
        assertThat(result.weightedTotal()).isEqualByComparingTo("90.0"); // 90*0.3 + 90*0.7 = 90
        assertThat(result.grade()).isEqualTo("A");
    }

    @Test
    void aMissingScoreForAnyoneOnTheRosterBlocksTheWholeSubmission() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B5", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        UUID scoredEnrollment = newEnrollment(year, schoolClass.id());
        UUID unscoredEnrollment = newEnrollment(year, schoolClass.id());

        var sba = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());

        scoreService.enterScoresBulk(sba.id(), List.of(ScoreEntry.scored(scoredEnrollment, new BigDecimal("90"))));
        scoreService.enterScoresBulk(exam.id(), List.of(ScoreEntry.scored(scoredEnrollment, new BigDecimal("90"))));
        // unscoredEnrollment deliberately left without any resolution for either component.

        assertThatThrownBy(() -> resultComputationService.computeAndSubmit(offeringId, termId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-007"));
    }

    @Test
    void sbaComponentWeightsNotSummingTo100BlockSubmission() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B6", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        newEnrollment(year, schoolClass.id());

        // Only 60% of the SBA category is defined — deliberately incomplete.
        assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("60"), LocalDate.now());
        assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());

        assertThatThrownBy(() -> resultComputationService.computeAndSubmit(offeringId, termId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-001"));
    }

    @Test
    void anExemptedComponentIsExcludedAndTheRemainderIsRescaledToA100Basis() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B7", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        var sba1 = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("20"), new BigDecimal("50"), LocalDate.now());
        var sba2 = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 2", AssessmentCategory.SBA, new BigDecimal("20"), new BigDecimal("50"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());

        scoreService.enterScoresBulk(sba1.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("15")))); // 75%
        scoreService.enterScoresBulk(sba2.id(), List.of(ScoreEntry.exempted(enrollmentId)));
        scoreService.enterScoresBulk(exam.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("60"))));

        List<TermResultView> results = resultComputationService.computeAndSubmit(offeringId, termId);

        // A-12: sba2 excluded from both numerator and denominator -> sbaTotal is just sba1's 75%,
        // rescaled over its own weight (50/50 = 100%), not zeroed or blocked.
        assertThat(results.get(0).sbaTotal()).isEqualByComparingTo("75.0");
    }

    @Test
    void subjectPositionsUseCompetitionRankingWithTies() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B8", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);

        UUID first = newEnrollment(year, schoolClass.id()); // 90
        UUID secondTiedA = newEnrollment(year, schoolClass.id()); // 80
        UUID secondTiedB = newEnrollment(year, schoolClass.id()); // 80
        UUID last = newEnrollment(year, schoolClass.id()); // 70

        var sba = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());

        // Equal SBA/EXAM per student makes weightedTotal == that student's flat percentage,
        // regardless of the 30/70 split, keeping the arithmetic trivially checkable.
        scoreService.enterScoresBulk(
                sba.id(),
                List.of(
                        ScoreEntry.scored(first, new BigDecimal("90")),
                        ScoreEntry.scored(secondTiedA, new BigDecimal("80")),
                        ScoreEntry.scored(secondTiedB, new BigDecimal("80")),
                        ScoreEntry.scored(last, new BigDecimal("70"))));
        scoreService.enterScoresBulk(
                exam.id(),
                List.of(
                        ScoreEntry.scored(first, new BigDecimal("90")),
                        ScoreEntry.scored(secondTiedA, new BigDecimal("80")),
                        ScoreEntry.scored(secondTiedB, new BigDecimal("80")),
                        ScoreEntry.scored(last, new BigDecimal("70"))));

        List<TermResultView> results = resultComputationService.computeAndSubmit(offeringId, termId);

        var byEnrollment = results.stream().collect(java.util.stream.Collectors.toMap(TermResultView::enrollmentId, r -> r));
        assertThat(byEnrollment.get(first).subjectPosition()).isEqualTo(1);
        assertThat(byEnrollment.get(secondTiedA).subjectPosition()).isEqualTo(2);
        assertThat(byEnrollment.get(secondTiedB).subjectPosition()).isEqualTo(2);
        assertThat(byEnrollment.get(last).subjectPosition()).isEqualTo(4);
    }

    @Test
    void resubmittingAnAlreadySubmittedSubjectTermIsRejected() {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass("B9", "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        var sba = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        scoreService.enterScoresBulk(sba.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("90"))));
        scoreService.enterScoresBulk(exam.id(), List.of(ScoreEntry.scored(enrollmentId, new BigDecimal("90"))));

        resultComputationService.computeAndSubmit(offeringId, termId);

        assertThatThrownBy(() -> resultComputationService.computeAndSubmit(offeringId, termId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-003"));
    }
}
