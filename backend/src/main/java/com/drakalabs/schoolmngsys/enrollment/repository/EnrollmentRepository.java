package com.drakalabs.schoolmngsys.enrollment.repository;

import com.drakalabs.schoolmngsys.enrollment.domain.Enrollment;
import com.drakalabs.schoolmngsys.enrollment.domain.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findByStudentIdAndAcademicYearIdAndStatus(UUID studentId, UUID academicYearId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndArchivedAtIsNullOrderByCreatedAtDesc(UUID studentId);

    List<Enrollment> findByClassIdAndAcademicYearIdAndStatusAndArchivedAtIsNull(
            UUID classId, UUID academicYearId, EnrollmentStatus status);

    List<Enrollment> findByAcademicYearIdAndStatusAndArchivedAtIsNull(UUID academicYearId, EnrollmentStatus status);
}
