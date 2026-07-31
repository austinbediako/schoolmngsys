package com.drakalabs.schoolmngsys.enrollment.service;

import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import com.drakalabs.schoolmngsys.enrollment.repository.EnrollmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-STU-03 (full history) / FR-ACAD-05 (class roster) — roster excludes exited students, history keeps everything. */
@Service
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentQueryService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> roster(UUID classId, UUID academicYearId) {
        return enrollmentRepository
                .findByClassIdAndAcademicYearIdAndStatusAndArchivedAtIsNull(classId, academicYearId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(EnrollmentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> history(UUID studentId) {
        return enrollmentRepository.findByStudentIdAndArchivedAtIsNullOrderByCreatedAtDesc(studentId).stream()
                .map(EnrollmentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> listByYearAndStatus(UUID academicYearId, EnrollmentStatus status) {
        return enrollmentRepository.findByAcademicYearIdAndStatusAndArchivedAtIsNull(academicYearId, status).stream()
                .map(EnrollmentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> listByYear(UUID academicYearId) {
        return listByYearAndStatus(academicYearId, EnrollmentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public EnrollmentView get(UUID enrollmentId) {
        return enrollmentRepository
                .findById(enrollmentId)
                .map(EnrollmentView::from)
                .orElseThrow(() -> new com.drakalabs.schoolmngsys.shared.web.error.NotFoundException("No such enrollment: " + enrollmentId));
    }
}
