package com.drakalabs.schoolmngsys.people.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** WP-3 test plan (docs/14 §5): "≥1 primary contact rule" (BR-EN-004) and student number (BR-EN-002/A-05). */
class StudentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    private GuardianView newGuardian(String phoneSuffix) {
        return guardianService.createGuardian("Ama", "Mensah", "+23320" + phoneSuffix, null, null, null);
    }

    @Test
    void creatingAStudentWithNoGuardiansFails() {
        assertThatThrownBy(
                        () -> studentService.createStudent(
                                "Kojo",
                                "Boateng",
                                null,
                                LocalDate.of(2018, 3, 4),
                                Gender.MALE,
                                LocalDate.of(2026, 9, 1),
                                List.of()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-004"));
    }

    @Test
    void creatingAStudentWithAGuardianButNoPrimaryContactFails() {
        GuardianView guardian = newGuardian("0000001");
        List<GuardianLinkSpec> links =
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, false, true, true, true));

        assertThatThrownBy(
                        () -> studentService.createStudent(
                                "Kojo", "Boateng", null, LocalDate.of(2018, 3, 4), Gender.MALE, LocalDate.of(2026, 9, 1), links))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-004"));
    }

    @Test
    void creatingAStudentWithAPrimaryContactGuardianSucceeds() {
        GuardianView guardian = newGuardian("0000002");
        List<GuardianLinkSpec> links =
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true));

        StudentView student = studentService.createStudent(
                "Kojo", "Boateng", null, LocalDate.of(2018, 3, 4), Gender.MALE, LocalDate.of(2026, 9, 1), links);

        assertThat(student.studentNumber()).startsWith("UBS-2026-");
    }

    @Test
    void studentNumbersAreSequentialWithinAnEntryYearAndNeverChangeableAfterCreation() {
        GuardianView guardianOne = newGuardian("0000003");
        GuardianView guardianTwo = newGuardian("0000004");
        List<GuardianLinkSpec> linksOne =
                List.of(new GuardianLinkSpec(guardianOne.id(), RelationshipType.FATHER, true, true, true, true));
        List<GuardianLinkSpec> linksTwo =
                List.of(new GuardianLinkSpec(guardianTwo.id(), RelationshipType.MOTHER, true, true, true, true));

        StudentView first = studentService.createStudent(
                "Ama", "Owusu", null, LocalDate.of(2019, 1, 1), Gender.FEMALE, LocalDate.of(2099, 9, 1), linksOne);
        StudentView second = studentService.createStudent(
                "Yaw", "Owusu", null, LocalDate.of(2019, 2, 2), Gender.MALE, LocalDate.of(2099, 9, 1), linksTwo);

        assertThat(first.studentNumber()).isNotEqualTo(second.studentNumber());
        assertThat(first.studentNumber()).startsWith("UBS-2099-");
        assertThat(second.studentNumber()).startsWith("UBS-2099-");

        // BR-EN-002: no service method exists to change studentNumber once assigned.
        StudentView updated = studentService.updateBio(first.id(), "Ama", "Boateng-Owusu", "Formerly Owusu");
        assertThat(updated.studentNumber()).isEqualTo(first.studentNumber());
    }

    @Test
    void admissionDetailsAreOptionalAtCreationAndCanBeSetAfterTheFact() {
        GuardianView guardian = newGuardian("0000005");
        List<GuardianLinkSpec> links = List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.FATHER, true, true, true, true));

        // The original 7-arg overload (no admission details) must keep working unchanged.
        StudentView created = studentService.createStudent(
                "Kwesi", "Asare", null, LocalDate.of(2018, 3, 4), Gender.MALE, LocalDate.of(2026, 9, 1), links);
        assertThat(created.nationality()).isNull();
        assertThat(created.emergencyContactName()).isNull();

        StudentAdmissionDetails details = new StudentAdmissionDetails(
                "Ghanaian", "Legon Presby Primary", "12 Legon Road, Accra", "Abena Asare", "+233201234567", "Mother");
        StudentView updated = studentService.updateAdmissionDetails(created.id(), details);

        assertThat(updated.nationality()).isEqualTo("Ghanaian");
        assertThat(updated.previousSchool()).isEqualTo("Legon Presby Primary");
        assertThat(updated.residentialAddress()).isEqualTo("12 Legon Road, Accra");
        assertThat(updated.emergencyContactName()).isEqualTo("Abena Asare");
        assertThat(updated.emergencyContactPhone()).isEqualTo("+233201234567");
        assertThat(updated.emergencyContactRelationship()).isEqualTo("Mother");
    }

    @Test
    void admissionDetailsCanBeSuppliedAtCreationViaTheEightArgOverload() {
        GuardianView guardian = newGuardian("0000006");
        List<GuardianLinkSpec> links = List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true));
        StudentAdmissionDetails details =
                new StudentAdmissionDetails("Nigerian", "Lagos Model School", "Osu, Accra", "Chidi Okafor", "+233209876543", "Father");

        StudentView created = studentService.createStudent(
                "Ngozi", "Okafor", null, LocalDate.of(2017, 6, 15), Gender.FEMALE, LocalDate.of(2026, 9, 1), links, details);

        assertThat(created.nationality()).isEqualTo("Nigerian");
        assertThat(created.previousSchool()).isEqualTo("Lagos Model School");
    }
}
