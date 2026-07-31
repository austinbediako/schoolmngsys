package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.ClassSubjectOffering;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSubjectOfferingRepository extends JpaRepository<ClassSubjectOffering, UUID> {

    Optional<ClassSubjectOffering> findBySchoolClassIdAndSubjectIdAndAcademicYearIdAndArchivedAtIsNull(
            UUID schoolClassId, UUID subjectId, UUID academicYearId);

    List<ClassSubjectOffering> findBySchoolClassIdAndAcademicYearIdAndArchivedAtIsNull(
            UUID schoolClassId, UUID academicYearId);
}
