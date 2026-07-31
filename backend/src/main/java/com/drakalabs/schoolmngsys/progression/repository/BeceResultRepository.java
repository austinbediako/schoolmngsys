package com.drakalabs.schoolmngsys.progression.repository;

import com.drakalabs.schoolmngsys.progression.domain.BeceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeceResultRepository extends JpaRepository<BeceResult, UUID> {

    List<BeceResult> findByBeceRegistrationIdAndArchivedAtIsNull(UUID beceRegistrationId);

    Optional<BeceResult> findByBeceRegistrationIdAndSubjectIdAndArchivedAtIsNull(UUID beceRegistrationId, UUID subjectId);
}
