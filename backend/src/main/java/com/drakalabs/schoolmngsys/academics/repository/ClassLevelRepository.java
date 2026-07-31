package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassLevelRepository extends JpaRepository<ClassLevel, UUID> {

    Optional<ClassLevel> findByCodeAndArchivedAtIsNull(String code);

    Optional<ClassLevel> findBySequenceAndArchivedAtIsNull(int sequence);

    java.util.List<ClassLevel> findAllByArchivedAtIsNullOrderBySequenceAsc();
}
