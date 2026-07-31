package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.TermResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermResultRepository extends JpaRepository<TermResult, UUID> {

    Optional<TermResult> findByEnrollmentIdAndClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(
            UUID enrollmentId, UUID classSubjectOfferingId, UUID termId);

    List<TermResult> findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(UUID classSubjectOfferingId, UUID termId);

    List<TermResult> findByEnrollmentIdAndTermIdAndArchivedAtIsNull(UUID enrollmentId, UUID termId);

    List<TermResult> findByEnrollmentIdAndArchivedAtIsNullOrderByCreatedAtDesc(UUID enrollmentId);

    /** Deliberately no archived filter — this is BR-AA-006's full revision history, superseded rows included. */
    List<TermResult> findByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);
}
