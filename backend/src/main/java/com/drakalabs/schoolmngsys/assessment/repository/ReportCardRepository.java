package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.ReportCard;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCardRepository extends JpaRepository<ReportCard, UUID> {

    Optional<ReportCard> findByEnrollmentIdAndTermIdAndArchivedAtIsNull(UUID enrollmentId, UUID termId);
}
