package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.SchoolClass;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<SchoolClass, UUID> {

    Optional<SchoolClass> findByClassLevelIdAndStreamAndArchivedAtIsNull(UUID classLevelId, String stream);

    List<SchoolClass> findByClassLevelIdAndArchivedAtIsNull(UUID classLevelId);
}
