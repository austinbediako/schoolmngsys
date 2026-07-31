package com.drakalabs.schoolmngsys.assessment.repository;

import com.drakalabs.schoolmngsys.assessment.domain.AssessmentCategory;
import com.drakalabs.schoolmngsys.assessment.domain.AssessmentComponent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentComponentRepository extends JpaRepository<AssessmentComponent, UUID> {

    List<AssessmentComponent> findByClassSubjectOfferingIdAndTermIdAndArchivedAtIsNull(UUID classSubjectOfferingId, UUID termId);

    List<AssessmentComponent> findByClassSubjectOfferingIdAndTermIdAndCategoryAndArchivedAtIsNull(
            UUID classSubjectOfferingId, UUID termId, AssessmentCategory category);
}
