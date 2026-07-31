package com.drakalabs.schoolmngsys.attendance.service;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceRecord;
import com.drakalabs.schoolmngsys.attendance.domain.AttendanceStatus;
import com.drakalabs.schoolmngsys.attendance.repository.AttendanceRecordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-AT-005/FR-ATT-04: LATE counts as present, EXCUSED counts as absent-with-reason (A-07). */
@Service
public class AttendanceSummaryService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceSummaryService(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryView summarize(UUID enrollmentId) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByEnrollmentIdAndArchivedAtIsNull(enrollmentId);

        long presentEquivalent =
                records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE).count();
        long absentEquivalent =
                records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT || r.getStatus() == AttendanceStatus.EXCUSED).count();

        return new AttendanceSummaryView(enrollmentId, records.size(), (int) presentEquivalent, (int) absentEquivalent);
    }
}
