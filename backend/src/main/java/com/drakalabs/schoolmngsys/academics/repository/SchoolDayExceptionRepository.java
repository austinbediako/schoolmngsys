package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.SchoolDayException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolDayExceptionRepository extends JpaRepository<SchoolDayException, UUID> {

    Optional<SchoolDayException> findByExceptionDateAndArchivedAtIsNull(LocalDate exceptionDate);
}
