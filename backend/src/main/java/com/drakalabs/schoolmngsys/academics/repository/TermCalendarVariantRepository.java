package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.TermCalendarVariant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermCalendarVariantRepository extends JpaRepository<TermCalendarVariant, UUID> {

    Optional<TermCalendarVariant> findByTermIdAndClassLevelIdAndArchivedAtIsNull(UUID termId, UUID classLevelId);
}
