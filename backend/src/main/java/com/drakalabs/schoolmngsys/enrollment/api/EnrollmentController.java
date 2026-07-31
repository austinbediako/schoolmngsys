package com.drakalabs.schoolmngsys.enrollment.api;

import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentQueryService enrollmentQueryService;

    public EnrollmentController(EnrollmentService enrollmentService, EnrollmentQueryService enrollmentQueryService) {
        this.enrollmentService = enrollmentService;
        this.enrollmentQueryService = enrollmentQueryService;
    }

    @PostMapping("/api/v1/enrollments")
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public EnrollmentResponse enroll(@RequestBody @Valid CreateEnrollmentRequest request) {
        return EnrollmentResponse.from(
                enrollmentService.enroll(request.studentId(), request.classId(), request.academicYearId(), request.rollNumber()));
    }

    @PostMapping("/api/v1/enrollments/{id}/exit")
    @PreAuthorize("hasAuthority('ENROLLMENT_END')")
    public EnrollmentResponse recordExit(@PathVariable UUID id, @RequestBody @Valid RecordExitRequest request) {
        return EnrollmentResponse.from(enrollmentService.recordExit(id, request.exitStatus(), request.reason(), request.exitDate()));
    }

    @GetMapping("/api/v1/students/{studentId}/enrollments")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public List<EnrollmentResponse> history(@PathVariable UUID studentId) {
        return enrollmentQueryService.history(studentId).stream().map(EnrollmentResponse::from).toList();
    }

    @GetMapping("/api/v1/classes/{classId}/roster")
    @PreAuthorize("hasAuthority('ROSTER_VIEW')")
    public List<EnrollmentResponse> roster(@PathVariable UUID classId, @RequestParam UUID academicYearId) {
        return enrollmentQueryService.roster(classId, academicYearId).stream().map(EnrollmentResponse::from).toList();
    }
}
