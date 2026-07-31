package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    Optional<AcademicYear> findByStatusAndArchivedAtIsNull(AcademicYearStatus status);

    Optional<AcademicYear> findByLabelAndArchivedAtIsNull(String label);
}
