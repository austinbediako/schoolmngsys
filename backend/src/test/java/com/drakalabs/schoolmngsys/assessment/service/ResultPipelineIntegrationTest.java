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
import com.drakalabs.schoolmngsys.assessment.domain.ResultStatus;
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

/** WP-6 (docs/14 §5): BR-AA-003 pipeline role gates and BR-AA-006 revision immutability. */
class ResultPipelineIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResultComputationService resultComputationService;

    @Autowired
    private ResultPipelineService resultPipelineService;

    @Autowired
    private ResultRevisionService resultRevisionService;

    @Autowired
    private ReportCardService reportCardService;

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

    private int phoneCounter = 6000000;

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
        GuardianView guardian = guardianService.createGuardian("Efua", "Mensah", "+23322" + (phoneCounter++), null, null, null);
        StudentView student = studentService.createStudent(
                "Kwesi",
                "Mensah",
                null,
                LocalDate.of(2016, 6, 1),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true)));
        return enrollmentService.enroll(student.id(), classId, year.id(), 1).id();
    }

    private record Fixture(UUID offeringId, UUID termId, UUID classId, UUID academicYearId, UUID enrollmentId) {
    }

    private Fixture submittedFixture(String classLevelCode, int score) {
        AcademicYearView year = newYearWithScale();
        ClassView schoolClass = classService.createClass(classLevelCode, "A-" + UUID.randomUUID(), 30);
        UUID offeringId = subjectOfferingService.createOffering(schoolClass.id(), mathsSubjectId(), year.id()).id();
        UUID termId = firstTermId(year);
        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        var sba = assessmentComponentService.createComponent(
                offeringId, termId, "SBA 1", AssessmentCategory.SBA, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        var exam = assessmentComponentService.createComponent(
                offeringId, termId, "End of Term Exam", AssessmentCategory.EXAM, new BigDecimal("100"), new BigDecimal("100"), LocalDate.now());
        scoreService.enterScoresBulk(sba.id(), List.of(ScoreEntry.scored(enrollmentId, BigDecimal.valueOf(score))));
        scoreService.enterScoresBulk(exam.id(), List.of(ScoreEntry.scored(enrollmentId, BigDecimal.valueOf(score))));
        resultComputationService.computeAndSubmit(offeringId, termId);

        return new Fixture(offeringId, termId, schoolClass.id(), year.id(), enrollmentId);
    }

    @Test
    void publishingBeforeApprovalIsRejected() {
        Fixture fixture = submittedFixture("B1", 80);

        assertThatThrownBy(() -> resultPipelineService.publishClassResults(fixture.classId(), fixture.academicYearId(), fixture.termId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-003"));
    }

    @Test
    void approvingTwiceIsRejectedSinceTheSecondCallFindsNoLongerSubmittedResults() {
        Fixture fixture = submittedFixture("B2", 80);
        resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId());

        assertThatThrownBy(() -> resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-003"));
    }

    @Test
    void fullPipelineProducesAPublishedResultAndAReportCard() {
        Fixture fixture = submittedFixture("B3", 80);

        resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId());
        List<TermResultView> published =
                resultPipelineService.publishClassResults(fixture.classId(), fixture.academicYearId(), fixture.termId());

        assertThat(published).allSatisfy(r -> assertThat(r.status()).isEqualTo(ResultStatus.PUBLISHED));

        ReportCardView reportCard = reportCardService.get(fixture.enrollmentId(), fixture.termId());
        assertThat(reportCard.classPosition()).isEqualTo(1);
        assertThat(reportCard.publishedAt()).isNotNull();
    }

    @Test
    void revisingANonPublishedResultIsRejected() {
        Fixture fixture = submittedFixture("B4", 80);
        // Only approved to HOD_APPROVED, never published — revision should still reject it.
        List<TermResultView> approved = resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId());
        UUID resultId = approved.get(0).id();

        assertThatThrownBy(() -> resultRevisionService.revise(resultId, "Data entry correction"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-006"));
    }

    @Test
    void revisionRequiresAReason() {
        Fixture fixture = submittedFixture("B6", 80);
        resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId());
        List<TermResultView> published =
                resultPipelineService.publishClassResults(fixture.classId(), fixture.academicYearId(), fixture.termId());
        UUID resultId = published.get(0).id();

        assertThatThrownBy(() -> resultRevisionService.revise(resultId, "  "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AA-006"));
    }

    @Test
    void revisingAPublishedResultArchivesTheOldRowAndCreatesAVersionedReplacement() {
        Fixture fixture = submittedFixture("B7", 80);
        resultPipelineService.approveSubjectResults(fixture.offeringId(), fixture.termId());
        List<TermResultView> published =
                resultPipelineService.publishClassResults(fixture.classId(), fixture.academicYearId(), fixture.termId());
        UUID originalId = published.get(0).id();

        TermResultView revised = resultRevisionService.revise(originalId, "Transcription error in exam score");

        assertThat(revised.id()).isNotEqualTo(originalId);
        assertThat(revised.resultVersion()).isEqualTo(2);
        assertThat(revised.status()).isEqualTo(ResultStatus.PUBLISHED);

        List<TermResultView> currentForEnrollment =
                resultRevisionServiceCurrent(fixture.enrollmentId(), fixture.termId());
        assertThat(currentForEnrollment).extracting(TermResultView::id).containsExactly(revised.id());
    }

    @Autowired
    private TermResultQueryService termResultQueryService;

    private List<TermResultView> resultRevisionServiceCurrent(UUID enrollmentId, UUID termId) {
        return termResultQueryService.currentForEnrollment(enrollmentId, termId);
    }
}
