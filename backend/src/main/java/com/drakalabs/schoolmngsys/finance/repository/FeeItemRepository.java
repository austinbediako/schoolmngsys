package com.drakalabs.schoolmngsys.finance.repository;

import com.drakalabs.schoolmngsys.finance.domain.FeeItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeItemRepository extends JpaRepository<FeeItem, UUID> {

    List<FeeItem> findByFeeScheduleIdAndArchivedAtIsNull(UUID feeScheduleId);
}
