package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.Score;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, UUID> {

    Optional<Score> findByAssessmentComponentIdAndEnrollmentIdAndArchivedAtIsNull(UUID assessmentComponentId, UUID enrollmentId);

    List<Score> findByAssessmentComponentIdInAndEnrollmentIdAndArchivedAtIsNull(List<UUID> assessmentComponentIds, UUID enrollmentId);
}
