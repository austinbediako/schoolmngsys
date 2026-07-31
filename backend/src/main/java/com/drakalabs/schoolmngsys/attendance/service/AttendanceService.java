package com.drakalabs.schoolmngsys.attendance.service;

import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.SchoolDayQueryService;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceRecord;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import com.drakalabs.schoolmngsys.attendance.repository.AttendanceRecordRepository;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily register (WF-03): one bulk submission per class per school day (BR-AT-001/002); after
 * submission, the register "locks" — a second bulk submission for the same class+date is
 * rejected, and further changes go through {@link #correctSameDay} (teacher, today only) or
 * {@link #correctPastRecord} (elevated permission, any date, reason mandatory — BR-AT-004).
 */
@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final ClassQueryService classQueryService;
    private final SchoolDayQueryService schoolDayQueryService;
    private final Clock clock;

    public AttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentQueryService enrollmentQueryService,
            ClassQueryService classQueryService,
            SchoolDayQueryService schoolDayQueryService,
            Clock clock) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentQueryService = enrollmentQueryService;
        this.classQueryService = classQueryService;
        this.schoolDayQueryService = schoolDayQueryService;
        this.clock = clock;
    }

    @Audited(action = "ATTENDANCE_MARKED", entityType = "AttendanceRecord")
    @Transactional
    public List<AttendanceRecordView> markRegister(
            UUID classId, UUID academicYearId, LocalDate date, List<AttendanceEntry> entries) {
        if (!date.isEqual(LocalDate.now(clock))) {
            throw new BusinessRuleViolationException(
                    "BR-AT-004", "Only today's register can be marked directly; use corrections for past dates");
        }

        ClassView schoolClass = classQueryService.get(classId);
        if (!schoolDayQueryService.isSchoolDay(date, schoolClass.classLevelCode(), academicYearId)) {
            throw new BusinessRuleViolationException("BR-AT-002", "Attendance may only be recorded on a school day");
        }

        List<EnrollmentView> roster = enrollmentQueryService.roster(classId, academicYearId);
        List<UUID> rosterEnrollmentIds = roster.stream().map(EnrollmentView::id).toList();

        boolean alreadyMarked =
                !attendanceRecordRepository.findByEnrollmentIdInAndAttendanceDateAndArchivedAtIsNull(rosterEnrollmentIds, date).isEmpty();
        if (alreadyMarked) {
            throw new BusinessRuleViolationException(
                    "BR-AT-001", "Attendance for this class and date has already been marked; use corrections instead");
        }

        for (AttendanceEntry entry : entries) {
            if (!rosterEnrollmentIds.contains(entry.enrollmentId())) {
                throw new BusinessRuleViolationException(
                        "BR-AT-001", "Enrollment " + entry.enrollmentId() + " is not on this class's active roster");
            }
        }

        return entries.stream()
                .map(entry -> attendanceRecordRepository.save(new AttendanceRecord(entry.enrollmentId(), date, entry.status())))
                .map(AttendanceRecordView::from)
                .toList();
    }

    /** Same-day self-correction — no elevated permission (WF-03: "same-day corrections by the teacher allowed"). */
    @Audited(action = "ATTENDANCE_CORRECTED_SAME_DAY", entityType = "AttendanceRecord")
    @Transactional
    public AttendanceRecordView correctSameDay(UUID recordId, AttendanceStatus newStatus) {
        AttendanceRecord record = getRecord(recordId);
        if (!record.getAttendanceDate().isEqual(LocalDate.now(clock))) {
            throw new BusinessRuleViolationException(
                    "BR-AT-004", "Only today's record can be corrected here; use the elevated-permission correction for past dates");
        }
        record.correct(newStatus, null);
        return AttendanceRecordView.from(attendanceRecordRepository.save(record));
    }

    /** BR-AT-004: post-day correction, elevated permission (enforced at the API layer) + mandatory reason. */
    @Audited(action = "ATTENDANCE_CORRECTED", entityType = "AttendanceRecord")
    @Transactional
    public AttendanceRecordView correctPastRecord(
            UUID recordId, AttendanceStatus newStatus, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("BR-AT-004", "A correction requires a reason");
        }
        AttendanceRecord record = getRecord(recordId);
        record.correct(newStatus, reason);
        return AttendanceRecordView.from(attendanceRecordRepository.save(record));
    }

    private AttendanceRecord getRecord(UUID recordId) {
        return attendanceRecordRepository
                .findById(recordId)
                .orElseThrow(() -> new NotFoundException("No such attendance record: " + recordId));
    }
}
