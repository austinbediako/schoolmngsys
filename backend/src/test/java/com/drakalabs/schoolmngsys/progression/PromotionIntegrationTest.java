package com.drakalabs.schoolmngsys.progression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionStatus;
import com.drakalabs.schoolmngsys.progression.domain.PromotionDecisionType;
import com.drakalabs.schoolmngsys.progression.domain.PromotionRunStatus;
import com.drakalabs.schoolmngsys.progression.service.ProgressionQueryService;
import com.drakalabs.schoolmngsys.progression.service.PromotionDecisionView;
import com.drakalabs.schoolmngsys.progression.service.PromotionRunView;
import com.drakalabs.schoolmngsys.progression.service.PromotionService;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-9 test plan (docs/14 §5):
 * Auto-promote default (A-08); repeat exception requiring justification + Head approval;
 * JHS 3 graduation default; bulk next-year enrollment generation; status gates.
 */
class PromotionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ProgressionQueryService progressionQueryService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentQueryService enrollmentQueryService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    private int counter = 8000000;

    private AcademicYearView createYear(String suffix) {
        return academicYearService.createYear(
                "Y-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 6),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
    }

    private StudentView createStudent(String first, String last) {
        GuardianView g = guardianService.createGuardian("Guardian", last, "+23320" + (counter++), null, null, null);
        return studentService.createStudent(
                first, last, null, LocalDate.of(2016, 1, 1), Gender.MALE, LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(g.id(), RelationshipType.FATHER, true, true, true, true)));
    }

    private UUID getLevelId(String code) {
        return classLevelRepository.findByCodeAndArchivedAtIsNull(code).orElseThrow().getId();
    }

    @Test
    void initiatePromotionRunAutoPromotesByDefaultAndGraduatesJhs3() {
        AcademicYearView year1 = createYear("1");
        AcademicYearView year2 = createYear("2");

        UUID b1LevelId = getLevelId("B1");
        UUID b9LevelId = getLevelId("B9");

        ClassView classB1 = classService.createClass("B1", "StreamA-" + UUID.randomUUID().toString().substring(0, 4), 30);
        ClassView classB9 = classService.createClass("B9", "StreamA-" + UUID.randomUUID().toString().substring(0, 4), 30);

        StudentView studentB1 = createStudent("Kofi", "Boateng");
        StudentView studentB9 = createStudent("Ama", "Owusu");

        enrollmentService.enroll(studentB1.id(), classB1.id(), year1.id(), null);
        enrollmentService.enroll(studentB9.id(), classB9.id(), year1.id(), null);

        PromotionRunView run = promotionService.initiateRun(year1.id(), year2.id());

        assertThat(run.status()).isEqualTo(PromotionRunStatus.PROPOSED);

        List<PromotionDecisionView> decisions = progressionQueryService.listDecisions(run.id());
        assertThat(decisions).hasSize(2);

        PromotionDecisionView decisionB1 = decisions.stream()
                .filter(d -> d.studentId().equals(studentB1.id()))
                .findFirst().orElseThrow();
        assertThat(decisionB1.decision()).isEqualTo(PromotionDecisionType.PROMOTE);
        assertThat(decisionB1.targetClassLevelId()).isEqualTo(getLevelId("B2"));

        PromotionDecisionView decisionB9 = decisions.stream()
                .filter(d -> d.studentId().equals(studentB9.id()))
                .findFirst().orElseThrow();
        assertThat(decisionB9.decision()).isEqualTo(PromotionDecisionType.GRADUATE);
        assertThat(decisionB9.targetClassLevelId()).isNull();
    }

    @Test
    void proposingRepeatWithoutJustificationFails() {
        AcademicYearView year1 = createYear("3");
        AcademicYearView year2 = createYear("4");

        ClassView classB1 = classService.createClass("B1", "StreamB-" + UUID.randomUUID().toString().substring(0, 4), 30);
        StudentView student = createStudent("Yaw", "Mensah");
        enrollmentService.enroll(student.id(), classB1.id(), year1.id(), null);

        PromotionRunView run = promotionService.initiateRun(year1.id(), year2.id());
        List<PromotionDecisionView> decisions = progressionQueryService.listDecisions(run.id());
        PromotionDecisionView decision = decisions.get(0);

        assertThatThrownBy(() -> promotionService.proposeException(decision.id(), PromotionDecisionType.REPEAT, null, ""))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-PR-002"));
    }

    @Test
    void executeRunBulkCreatesNextYearEnrollmentsWhenApproved() {
        AcademicYearView year1 = createYear("5");
        AcademicYearView year2 = createYear("6");

        UUID b1LevelId = getLevelId("B1");
        UUID b2LevelId = getLevelId("B2");

        String streamName = "StreamC-" + UUID.randomUUID().toString().substring(0, 4);
        ClassView classB1Year1 = classService.createClass("B1", streamName, 30);
        ClassView classB2Year2 = classService.createClass("B2", streamName, 30);

        StudentView student1 = createStudent("Kwaku", "Appiah");
        StudentView student2 = createStudent("Akosua", "Appiah");

        enrollmentService.enroll(student1.id(), classB1Year1.id(), year1.id(), null);
        enrollmentService.enroll(student2.id(), classB1Year1.id(), year1.id(), null);

        PromotionRunView run = promotionService.initiateRun(year1.id(), year2.id());
        List<PromotionDecisionView> decisions = progressionQueryService.listDecisions(run.id());

        // Propose repeat for student2 with justification
        PromotionDecisionView d2 = decisions.stream().filter(d -> d.studentId().equals(student2.id())).findFirst().orElseThrow();
        promotionService.proposeException(d2.id(), PromotionDecisionType.REPEAT, b1LevelId, "Failed basic literacy requirements");

        // Attempting execute before approval fails
        assertThatThrownBy(() -> promotionService.executeRun(run.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-PR-002"));

        // Approve run
        promotionService.approveRun(run.id());

        // Execute run
        PromotionRunView executedRun = promotionService.executeRun(run.id());
        assertThat(executedRun.status()).isEqualTo(PromotionRunStatus.EXECUTED);

        // Verify next year's enrollments created
        List<EnrollmentView> year2Enrollments = enrollmentQueryService.listByYear(year2.id());
        assertThat(year2Enrollments).hasSize(2);

        EnrollmentView e1Next = year2Enrollments.stream().filter(e -> e.studentId().equals(student1.id())).findFirst().orElseThrow();
        assertThat(e1Next.classId()).isEqualTo(classB2Year2.id()); // Promoted to B2

        EnrollmentView e2Next = year2Enrollments.stream().filter(e -> e.studentId().equals(student2.id())).findFirst().orElseThrow();
        assertThat(e2Next.classId()).isEqualTo(classB1Year1.id()); // Repeat in B1
    }
}
