package com.drakalabs.schoolmngsys.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceRecord;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import com.drakalabs.schoolmngsys.attendance.repository.AttendanceRecordRepository;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * WP-5 test plan (docs/14 §5): "non-school-day rejection; duplicate-day rejection; LATE/EXCUSED
 * aggregation semantics (A-07); post-day correction requires permission + reason."
 *
 * <p>Uses a fixed {@link Clock} (a known Thursday) instead of the real system clock so
 * "mark today's register" logic is deterministic regardless of which real-world day the suite
 * runs on.
 */
@Import(AttendanceServiceIntegrationTest.FixedClockConfig.class)
class AttendanceServiceIntegrationTest extends AbstractIntegrationTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 7, 30); // a Thursday

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(FIXED_TODAY.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        }
    }

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceSummaryService attendanceSummaryService;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private EnrollmentService enrollmentService;

    private AcademicYearView yearCovering(int startYear, int endYear) {
        return academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(startYear, 1, 1),
                LocalDate.of(endYear, 12, 31),
                List.of(
                        new TermSpec(1, LocalDate.of(startYear, 1, 1), LocalDate.of(startYear, 4, 30), 70),
                        new TermSpec(2, LocalDate.of(startYear, 5, 1), LocalDate.of(startYear, 8, 31), 70),
                        new TermSpec(3, LocalDate.of(startYear, 9, 1), LocalDate.of(endYear, 12, 31), 70)));
    }

    private StudentView newStudent(String phoneSuffix) {
        GuardianView guardian = guardianService.createGuardian("Adwoa", "Frimpong", "+23320" + phoneSuffix, null, null, null);
        return studentService.createStudent(
                "Kwaku",
                "Frimpong",
                null,
                LocalDate.of(2017, 3, 1),
                Gender.MALE,
                LocalDate.of(2026, 1, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true)));
    }

    @Test
    void markingAttendanceOnANonSchoolDayIsRejected() {
        AcademicYearView year = yearCovering(2020, 2020); // doesn't cover FIXED_TODAY (2026)
        ClassView schoolClass = classService.createClass("B1", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("1000001");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);

        assertThatThrownBy(
                        () -> attendanceService.markRegister(
                                schoolClass.id(),
                                year.id(),
                                FIXED_TODAY,
                                List.of(new AttendanceEntry(enrollment.id(), AttendanceStatus.PRESENT))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AT-002"));
    }

    @Test
    void markingTheSameClassAndDateTwiceIsRejected() {
        AcademicYearView year = yearCovering(2026, 2026);
        ClassView schoolClass = classService.createClass("B2", "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent("1000002");
        EnrollmentView enrollment = enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1);

        attendanceService.markRegister(
                schoolClass.id(), year.id(), FIXED_TODAY, List.of(new AttendanceEntry(enrollment.id(), AttendanceStatus.PRESENT)));

        assertThatThrownBy(
                        () -> attendanceService.markRegister(
                                schoolClass.id(),
                                year.id(),
                                FIXED_TODAY,
                                List.of(new AttendanceEntry(enrollment.id(), AttendanceStatus.ABSENT))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AT-001"));
    }

    private UUID newEnrollmentId(String phoneSuffix, String classLevelCode) {
        AcademicYearView year = yearCovering(2026, 2026);
        ClassView schoolClass = classService.createClass(classLevelCode, "A-" + UUID.randomUUID(), 30);
        StudentView student = newStudent(phoneSuffix);
        return enrollmentService.enroll(student.id(), schoolClass.id(), year.id(), 1).id();
    }

    @Test
    void lateCountsAsPresentAndExcusedCountsAsAbsentInAggregates() {
        UUID enrollmentId = newEnrollmentId("1000003", "B3");
        attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, LocalDate.of(2026, 1, 5), AttendanceStatus.PRESENT));
        attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, LocalDate.of(2026, 1, 6), AttendanceStatus.LATE));
        attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, LocalDate.of(2026, 1, 7), AttendanceStatus.ABSENT));
        attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, LocalDate.of(2026, 1, 8), AttendanceStatus.EXCUSED));

        AttendanceSummaryView summary = attendanceSummaryService.summarize(enrollmentId);

        assertThat(summary.totalSchoolDays()).isEqualTo(4);
        assertThat(summary.presentEquivalent()).isEqualTo(2); // PRESENT + LATE
        assertThat(summary.absentEquivalent()).isEqualTo(2); // ABSENT + EXCUSED
    }

    @Test
    void correctingAPastRecordThroughTheSameDayPathFails() {
        UUID enrollmentId = newEnrollmentId("1000004", "B4");
        AttendanceRecord pastRecord =
                attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, FIXED_TODAY.minusDays(1), AttendanceStatus.PRESENT));

        assertThatThrownBy(() -> attendanceService.correctSameDay(pastRecord.getId(), AttendanceStatus.ABSENT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AT-004"));
    }

    @Test
    void correctingAPastRecordWithoutAReasonFails() {
        UUID enrollmentId = newEnrollmentId("1000005", "B5");
        AttendanceRecord pastRecord =
                attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, FIXED_TODAY.minusDays(1), AttendanceStatus.PRESENT));

        assertThatThrownBy(() -> attendanceService.correctPastRecord(pastRecord.getId(), AttendanceStatus.ABSENT, "  "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(ex -> assertThat(((BusinessRuleViolationException) ex).getRuleId()).isEqualTo("BR-AT-004"));
    }

    @Test
    void correctingAPastRecordWithAReasonSucceeds() {
        UUID enrollmentId = newEnrollmentId("1000006", "B6");
        AttendanceRecord pastRecord =
                attendanceRecordRepository.save(new AttendanceRecord(enrollmentId, FIXED_TODAY.minusDays(1), AttendanceStatus.PRESENT));

        AttendanceRecordView corrected =
                attendanceService.correctPastRecord(pastRecord.getId(), AttendanceStatus.ABSENT, "Confirmed absent by class teacher");

        assertThat(corrected.status()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(corrected.correctionReason()).isEqualTo("Confirmed absent by class teacher");
    }
}
