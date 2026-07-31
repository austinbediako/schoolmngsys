package com.drakalabs.schoolmngsys.people.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WP-3 test plan (docs/14 §5): "guardian-ward resolution correctness (feeds all guardian
 * scoping)" — the mechanism every future module's guardian scope filter is built on.
 */
class GuardianWardResolutionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private StudentGuardianLinkService studentGuardianLinkService;

    @Autowired
    private GuardianWardResolutionService guardianWardResolutionService;

    @Test
    void resolvesAllWardsLinkedToAGuardianAcrossMultipleChildren() {
        GuardianView parent = guardianService.createGuardian("Abena", "Owusu", "+23327" + "7000001", null, null, null);
        GuardianView unrelated = guardianService.createGuardian("Kojo", "Sarpong", "+23327" + "7000002", null, null, null);

        StudentView firstChild = studentService.createStudent(
                "Nana",
                "Owusu",
                null,
                LocalDate.of(2016, 1, 1),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(parent.id(), RelationshipType.MOTHER, true, true, true, true)));
        StudentView secondChild = studentService.createStudent(
                "Adjoa",
                "Owusu",
                null,
                LocalDate.of(2018, 2, 2),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(parent.id(), RelationshipType.MOTHER, true, true, true, true)));
        StudentView unrelatedChild = studentService.createStudent(
                "Yaw",
                "Sarpong",
                null,
                LocalDate.of(2017, 3, 3),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(unrelated.id(), RelationshipType.FATHER, true, true, true, true)));

        Set<UUID> wards = guardianWardResolutionService.resolveWardIds(parent.id());

        assertThat(wards).containsExactlyInAnyOrder(firstChild.id(), secondChild.id());
        assertThat(wards).doesNotContain(unrelatedChild.id());
        assertThat(guardianWardResolutionService.isWardOf(parent.id(), firstChild.id())).isTrue();
        assertThat(guardianWardResolutionService.isWardOf(parent.id(), unrelatedChild.id())).isFalse();
    }

    @Test
    void guardianWithNoLinksHasNoWards() {
        GuardianView lonelyGuardian = guardianService.createGuardian("Esi", "Amankwah", "+23327" + "7000003", null, null, null);

        assertThat(guardianWardResolutionService.resolveWardIds(lonelyGuardian.id())).isEmpty();
    }

    @Test
    void resolutionExcludesUnlinkedWards() {
        GuardianView guardianOne = guardianService.createGuardian("Kofi", "Boadi", "+23327" + "7000004", null, null, null);
        GuardianView guardianTwo = guardianService.createGuardian("Akosua", "Boadi", "+23327" + "7000005", null, null, null);
        StudentView student = studentService.createStudent(
                "Kwabena",
                "Boadi",
                null,
                LocalDate.of(2015, 4, 4),
                Gender.MALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardianOne.id(), RelationshipType.FATHER, true, true, true, true)));
        studentGuardianLinkService.link(student.id(), guardianTwo.id(), RelationshipType.MOTHER, true, true, false, true);

        studentGuardianLinkService.unlink(student.id(), guardianOne.id());

        assertThat(guardianWardResolutionService.resolveWardIds(guardianOne.id())).isEmpty();
        assertThat(guardianWardResolutionService.resolveWardIds(guardianTwo.id())).containsExactly(student.id());
    }
}
