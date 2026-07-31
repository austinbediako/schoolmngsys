package com.drakalabs.schoolmngsys.attendance.api;

import com.drakalabs.schoolmngsys.attendance.service.AttendanceEntry;
import com.drakalabs.schoolmngsys.attendance.service.AttendanceService;
import com.drakalabs.schoolmngsys.attendance.service.AttendanceSummaryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceSummaryService attendanceSummaryService;

    public AttendanceController(AttendanceService attendanceService, AttendanceSummaryService attendanceSummaryService) {
        this.attendanceService = attendanceService;
        this.attendanceSummaryService = attendanceSummaryService;
    }

    @PostMapping("/api/v1/attendance/registers")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public List<AttendanceRecordResponse> markRegister(@RequestBody @Valid MarkRegisterRequest request) {
        List<AttendanceEntry> entries =
                request.entries().stream().map(e -> new AttendanceEntry(e.enrollmentId(), e.status())).toList();
        return attendanceService.markRegister(request.classId(), request.academicYearId(), request.date(), entries).stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }

    @PutMapping("/api/v1/attendance/records/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public AttendanceRecordResponse correctSameDay(@PathVariable UUID id, @RequestBody @Valid CorrectAttendanceRequest request) {
        return AttendanceRecordResponse.from(attendanceService.correctSameDay(id, request.status()));
    }

    @PutMapping("/api/v1/attendance/records/{id}/correct")
    @PreAuthorize("hasAuthority('ATTENDANCE_CORRECT')")
    public AttendanceRecordResponse correctPastRecord(@PathVariable UUID id, @RequestBody @Valid CorrectPastAttendanceRequest request) {
        return AttendanceRecordResponse.from(attendanceService.correctPastRecord(id, request.status(), request.reason()));
    }

    @GetMapping("/api/v1/attendance/summaries/{enrollmentId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW')")
    public AttendanceSummaryResponse summary(@PathVariable UUID enrollmentId) {
        return AttendanceSummaryResponse.from(attendanceSummaryService.summarize(enrollmentId));
    }
}
