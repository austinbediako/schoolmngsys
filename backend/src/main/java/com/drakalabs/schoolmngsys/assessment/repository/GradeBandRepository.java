package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.GradeBand;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeBandRepository extends JpaRepository<GradeBand, UUID> {

    List<GradeBand> findByGradeScaleIdAndArchivedAtIsNull(UUID gradeScaleId);
}
