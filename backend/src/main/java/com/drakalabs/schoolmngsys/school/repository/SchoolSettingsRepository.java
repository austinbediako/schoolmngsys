package com.drakalabs.schoolmngsys.school.repository;

import com.drakalabs.schoolmngsys.school.domain.SchoolSettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, UUID> {

    Optional<SchoolSettings> findFirstByArchivedAtIsNull();
}
