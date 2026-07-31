package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.ClassTeacherAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTeacherAssignmentRepository extends JpaRepository<ClassTeacherAssignment, UUID> {

    Optional<ClassTeacherAssignment> findBySchoolClassIdAndAcademicYearIdAndArchivedAtIsNull(
            UUID schoolClassId, UUID academicYearId);

    Optional<ClassTeacherAssignment> findByTeacherStaffIdAndAcademicYearIdAndArchivedAtIsNull(
            UUID teacherStaffId, UUID academicYearId);
}
