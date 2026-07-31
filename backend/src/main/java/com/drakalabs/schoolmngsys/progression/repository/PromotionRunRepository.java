package com.drakalabs.schoolmngsys.progression.repository;

import com.drakalabs.schoolmngsys.progression.domain.PromotionRun;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRunRepository extends JpaRepository<PromotionRun, UUID> {

    Optional<PromotionRun> findBySourceAcademicYearIdAndArchivedAtIsNull(UUID sourceAcademicYearId);
}
