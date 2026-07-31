package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.GradeScale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeScaleRepository extends JpaRepository<GradeScale, UUID> {

    Optional<GradeScale> findByAcademicYearIdAndArchivedAtIsNull(UUID academicYearId);
}
