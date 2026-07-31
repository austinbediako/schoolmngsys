package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.FeeSchedule;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, UUID> {

    Optional<FeeSchedule> findByClassLevelIdAndTermIdAndArchivedAtIsNull(UUID classLevelId, UUID termId);
}
