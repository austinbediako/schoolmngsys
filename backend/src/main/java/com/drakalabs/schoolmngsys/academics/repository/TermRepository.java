package com.drakalabs.schoolmngsys.academics.repository;

import com.drakalabs.schoolmngsys.academics.domain.Term;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, UUID> {

    List<Term> findByAcademicYearIdAndArchivedAtIsNullOrderByTermNumber(UUID academicYearId);

    Optional<Term> findByAcademicYearIdAndTermNumberAndArchivedAtIsNull(UUID academicYearId, int termNumber);
}
