package com.drakalabs.schoolmngsys.progression.repository;

import com.drakalabs.schoolmngsys.progression.domain.BeceRegistration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeceRegistrationRepository extends JpaRepository<BeceRegistration, UUID> {

    Optional<BeceRegistration> findByEnrollmentIdAndArchivedAtIsNull(UUID enrollmentId);

    Optional<BeceRegistration> findByIndexNumberAndArchivedAtIsNull(String indexNumber);
}
