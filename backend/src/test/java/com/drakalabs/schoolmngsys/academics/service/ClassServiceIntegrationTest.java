package com.drakalabs.schoolmngsys.academics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** WP-2 test plan (docs/14 §5): "one-class-teacher-per-year" (BR-AS-005/A-01). */
class ClassServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClassService classService;

    @Autowired
    private AcademicYearService academicYearService;

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

    @Test
    void assigningASecondClassTeacherToTheSameClassInTheSameYearFails() {
        AcademicYearView year = newYear();
        ClassView schoolClass = classService.createClass("B1", "A-" + UUID.randomUUID(), 30);
        UUID firstTeacher = UUID.randomUUID();
        UUID secondTeacher = UUID.randomUUID();

        classService.assignClassTeacher(schoolClass.id(), year.id(), firstTeacher);

        assertThatThrownBy(() -> classService.assignClassTeacher(schoolClass.id(), year.id(), secondTeacher))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AS-005"));
    }

    @Test
    void aTeacherCannotClassTeachTwoClassesInTheSameYear() {
        AcademicYearView year = newYear();
        ClassView classA = classService.createClass("B2", "A-" + UUID.randomUUID(), 30);
        ClassView classB = classService.createClass("B2", "B-" + UUID.randomUUID(), 30);
        UUID teacher = UUID.randomUUID();

        classService.assignClassTeacher(classA.id(), year.id(), teacher);

        assertThatThrownBy(() -> classService.assignClassTeacher(classB.id(), year.id(), teacher))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AS-005"));
    }

    @Test
    void aTeacherMayClassTeachDifferentClassesInDifferentYears() {
        AcademicYearView yearOne = academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2020, 9, 1),
                LocalDate.of(2021, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2020, 9, 1), LocalDate.of(2020, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2021, 1, 5), LocalDate.of(2021, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2021, 4, 25), LocalDate.of(2021, 8, 1), 65)));
        AcademicYearView yearTwo = newYear();
        ClassView classA = classService.createClass("B3", "A-" + UUID.randomUUID(), 30);
        ClassView classB = classService.createClass("B3", "B-" + UUID.randomUUID(), 30);
        UUID teacher = UUID.randomUUID();

        classService.assignClassTeacher(classA.id(), yearOne.id(), teacher);

        assertThat(classService.assignClassTeacher(classB.id(), yearTwo.id(), teacher).teacherStaffId()).isEqualTo(teacher);
    }
}
