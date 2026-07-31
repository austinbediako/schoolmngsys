package com.drakalabs.schoolmngsys.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.domain.StudentStatus;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentQueryService;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-4 test plan (docs/14 §5): "one-active-enrollment-per-year (DB + service); mid-year
 * transfer-in/out; roster excludes exited students but history intact."
 */
class EnrollmentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentQueryService enrollmentQueryService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private StudentQueryService studentQueryService;

    private AcademicYearView newYear() {
        return academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
    }

    private StudentView newStudent(String phoneSuffix) {
        GuardianView guardian = guardianService.createGuardian("Yaa", "Asante", "+23328" + phoneSuffix, null, null, null);
        return studentService.createStudent(
                "Ama",
                "Asante",
                null,
                LocalDate.of(2018, 6, 1),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true)));
    }

    @Test
    void enrollingAStudentSucceedsAndAppearsOnTheRoster() {
        AcademicYearView year = newYear();
        ClassView schoolClass = classService.createClass("B1", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("9000001");

        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);

        assertThat(enrollment.status()).isEqualTo(EnrollmentStatus.ACTIVE);
        List<EnrollmentView> roster = enrollmentQueryService.roster(schoolClass.id(), year.id());
        assertThat(roster).extracting(EnrollmentView::studentId).contains(student.id());
    }

    @Test
    void aStudentCannotHaveTwoActiveEnrollmentsInTheSameYear() {
        AcademicYearView year = newYear();
        ClassView classA = classService.createClass("B2", "A-" + UUID.randomUUID(), 30);
        ClassView classB = classService.createClass("B2", "B-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("9000002");

        enrollmentService.enroll(student.id(), classA.id(), year.id(), 1);

        assertThatThrownBy(() -> enrollmentService.enroll(student.id(), classB.id(), year.id(), 1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-001"));
    }

    @Test
    void transferOutRemovesFromRosterButKeepsHistoryAndUpdatesStudentStatus() {
        AcademicYearView year = newYear();
        ClassView schoolClass = classService.createClass("B3", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("9000003");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);

        EnrollmentView exited = enrollmentService.recordExit(
                enrollment.id(), EnrollmentStatus.TRANSFERRED, "Family relocated", LocalDate.of(2026, 11, 1));

        assertThat(exited.status()).isEqualTo(EnrollmentStatus.TRANSFERRED);
        assertThat(exited.exitReason()).isEqualTo("Family relocated");

        List<EnrollmentView> roster = enrollmentQueryService.roster(schoolClass.id(), year.id());
        assertThat(roster).extracting(EnrollmentView::studentId).doesNotContain(student.id());

        List<EnrollmentView> history = enrollmentQueryService.history(student.id());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).status()).isEqualTo(EnrollmentStatus.TRANSFERRED);

        assertThat(studentQueryService.get(student.id()).status()).isEqualTo(StudentStatus.TRANSFERRED_OUT);
    }

    @Test
    void withdrawalRemovesFromRosterButKeepsHistoryAndUpdatesStudentStatus() {
        AcademicYearView year = newYear();
        ClassView schoolClass = classService.createClass("B4", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("9000004");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);

        enrollmentService.recordExit(enrollment.id(), EnrollmentStatus.WITHDRAWN, "Moved to another school", LocalDate.of(2026, 10, 15));

        assertThat(enrollmentQueryService.roster(schoolClass.id(), year.id())).isEmpty();
        assertThat(studentQueryService.get(student.id()).status()).isEqualTo(StudentStatus.WITHDRAWN);
    }

    @Test
    void aStudentMayBeEnrolledMidYearAfterATransferOutFromAnotherClass() {
        AcademicYearView year = newYear();
        ClassView oldClass = classService.createClass("B5", "A-" + UUID.randomUUID(), 30);
        ClassView newClass = classService.createClass("B5", "B-" + UUID.randomUUID(), 30);
        StudentView leaver = newStudent("9000005");
        StudentView incoming = newStudent("9000006");

        EnrollmentView leaverEnrollment = enrollmentService.enroll(leaver.id(), oldClass.id(), year.id(), 1);
        enrollmentService.recordExit(leaverEnrollment.id(), EnrollmentStatus.TRANSFERRED, "Relocated", LocalDate.of(2026, 10, 1));

        EnrollmentView incomingEnrollment = enrollmentService.enroll(incoming.id(), newClass.id(), year.id(), 1);

        assertThat(incomingEnrollment.status()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollmentQueryService.roster(newClass.id(), year.id())).extracting(EnrollmentView::studentId).contains(incoming.id());
    }

    @Test
    void exitingAnAlreadyExitedEnrollmentFails() {
        AcademicYearView year = newYear();
        ClassView schoolClass = classService.createClass("B6", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("9000007");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);
        enrollmentService.recordExit(enrollment.id(), EnrollmentStatus.WITHDRAWN, "Left", LocalDate.of(2026, 10, 1));

        assertThatThrownBy(
                        () -> enrollmentService.recordExit(enrollment.id(), EnrollmentStatus.WITHDRAWN, "Left again", LocalDate.of(2026, 10, 2)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-003"));
    }
}
