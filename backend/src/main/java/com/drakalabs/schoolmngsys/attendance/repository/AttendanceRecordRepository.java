package com.drakalabs.schoolmngsys.attendance.repository;

import com.drakalabs.schoolmngsys.attendance.domain.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByEnrollmentIdAndAttendanceDateAndArchivedAtIsNull(UUID enrollmentId, LocalDate attendanceDate);

    List<AttendanceRecord> findByEnrollmentIdInAndAttendanceDateAndArchivedAtIsNull(List<UUID> enrollmentIds, LocalDate attendanceDate);

    List<AttendanceRecord> findByEnrollmentIdAndArchivedAtIsNull(UUID enrollmentId);
}
