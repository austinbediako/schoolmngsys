package com.drakalabs.schoolmngsys.enrollment.service;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.enrollment.domain.Enrollment;
import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import com.drakalabs.schoolmngsys.enrollment.repository.EnrollmentRepository;
import com.drakalabs.schoolmngsys.people.service.StudentQueryService;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BR-EN-001 (at most one ACTIVE enrollment per student per year) and BR-EN-005 (exits need a
 * recorded reason and date, and update the student's overall status via {@code people}'s service
 * — enrollment depends on {@code people}/{@code academics} per docs/08 §3, never the reverse).
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentQueryService studentQueryService;
    private final StudentService studentService;
    private final ClassQueryService classQueryService;
    private final AcademicYearQueryService academicYearQueryService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentQueryService studentQueryService,
            StudentService studentService,
            ClassQueryService classQueryService,
            AcademicYearQueryService academicYearQueryService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentQueryService = studentQueryService;
        this.studentService = studentService;
        this.classQueryService = classQueryService;
        this.academicYearQueryService = academicYearQueryService;
    }

    @Audited(action = "ENROLLMENT_CREATED", entityType = "Enrollment")
    @Transactional
    public EnrollmentView enroll(UUID studentId, UUID classId, UUID academicYearId, Integer rollNumber) {
        studentQueryService.get(studentId);
        classQueryService.get(classId);
        academicYearQueryService.get(academicYearId);

        enrollmentRepository
                .findByStudentIdAndAcademicYearIdAndStatus(studentId, academicYearId, EnrollmentStatus.ACTIVE)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-EN-001", "This student already has an active enrollment for this academic year");
                        });

        return EnrollmentView.from(enrollmentRepository.save(new Enrollment(studentId, classId, academicYearId, rollNumber)));
    }

    /** BR-EN-005: {@code exitStatus} must be TRANSFERRED or WITHDRAWN — never a raw status jump. */
    @Audited(action = "ENROLLMENT_EXITED", entityType = "Enrollment")
    @Transactional
    public EnrollmentView recordExit(UUID enrollmentId, EnrollmentStatus exitStatus, String reason, LocalDate exitDate) {
        if (exitStatus != EnrollmentStatus.TRANSFERRED && exitStatus != EnrollmentStatus.WITHDRAWN) {
            throw new BusinessRuleViolationException("BR-EN-005", "Exit status must be TRANSFERRED or WITHDRAWN");
        }

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("No such enrollment: " + enrollmentId));
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessRuleViolationException("BR-EN-003", "Only an active enrollment can be exited");
        }

        enrollment.exit(exitStatus, reason, exitDate);
        enrollmentRepository.save(enrollment);

        if (exitStatus == EnrollmentStatus.TRANSFERRED) {
            studentService.markTransferredOut(enrollment.getStudentId());
        } else {
            studentService.markWithdrawn(enrollment.getStudentId());
        }

        return EnrollmentView.from(enrollment);
    }
}
