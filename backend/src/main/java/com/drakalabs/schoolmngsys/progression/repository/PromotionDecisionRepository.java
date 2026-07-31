package com.drakalabs.schoolmngsys.progression.repository;

import com.drakalabs.schoolmngsys.progression.domain.PromotionDecision;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionDecisionRepository extends JpaRepository<PromotionDecision, UUID> {

    List<PromotionDecision> findByPromotionRunIdAndArchivedAtIsNull(UUID promotionRunId);

    Optional<PromotionDecision> findByPromotionRunIdAndStudentIdAndArchivedAtIsNull(UUID promotionRunId, UUID studentId);
}
