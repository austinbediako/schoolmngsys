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

/** WP-3 test plan (docs/14 §5): BR-EN-004 must also hold when guardian links are removed. */
class StudentGuardianLinkServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private StudentGuardianLinkService studentGuardianLinkService;

    @Autowired
    private StudentQueryService studentQueryService;

    private GuardianView newGuardian(String phoneSuffix) {
        return guardianService.createGuardian("Kwame", "Asare", "+23324" + phoneSuffix, null, null, null);
    }

    @Test
    void unlinkingTheOnlyGuardianFails() {
        GuardianView guardian = newGuardian("1000001");
        StudentView student = studentService.createStudent(
                "Efua",
                "Danso",
                null,
                LocalDate.of(2017, 5, 6),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true)));

        assertThatThrownBy(() -> studentGuardianLinkService.unlink(student.id(), guardian.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-004"));
    }

    @Test
    void unlinkingTheLastPrimaryContactFailsEvenIfAnotherGuardianRemains() {
        GuardianView primary = newGuardian("1000002");
        GuardianView secondary = newGuardian("1000003");
        StudentView student = studentService.createStudent(
                "Efua",
                "Danso",
                null,
                LocalDate.of(2017, 5, 6),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(primary.id(), RelationshipType.MOTHER, true, true, true, true)));
        studentGuardianLinkService.link(student.id(), secondary.id(), RelationshipType.FATHER, false, true, false, true);

        assertThatThrownBy(() -> studentGuardianLinkService.unlink(student.id(), primary.id()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-EN-004"));
    }

    @Test
    void unlinkingANonPrimaryGuardianWhenAPrimaryRemainsSucceeds() {
        GuardianView primary = newGuardian("1000004");
        GuardianView secondary = newGuardian("1000005");
        StudentView student = studentService.createStudent(
                "Efua",
                "Danso",
                null,
                LocalDate.of(2017, 5, 6),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(primary.id(), RelationshipType.MOTHER, true, true, true, true)));
        studentGuardianLinkService.link(student.id(), secondary.id(), RelationshipType.FATHER, false, true, false, true);

        studentGuardianLinkService.unlink(student.id(), secondary.id());

        List<StudentGuardianView> remaining = studentQueryService.listGuardianLinks(student.id());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).guardianId()).isEqualTo(primary.id());
    }
}
