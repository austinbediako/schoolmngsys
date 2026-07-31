package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.FeeItem;
import com.drakalabs.schoolmngsys.finance.domain.FeeSchedule;
import com.drakalabs.schoolmngsys.finance.domain.FeeScheduleStatus;
import com.drakalabs.schoolmngsys.finance.repository.FeeItemRepository;
import com.drakalabs.schoolmngsys.finance.repository.FeeScheduleRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-FI-001: one schedule per (ClassLevel, Term); must be APPROVED (Head) before billing. */
@Service
public class FeeScheduleService {

    private final FeeScheduleRepository feeScheduleRepository;
    private final FeeItemRepository feeItemRepository;

    public FeeScheduleService(FeeScheduleRepository feeScheduleRepository, FeeItemRepository feeItemRepository) {
        this.feeScheduleRepository = feeScheduleRepository;
        this.feeItemRepository = feeItemRepository;
    }

    @Audited(action = "FEE_SCHEDULE_CREATED", entityType = "FeeSchedule")
    @Transactional
    public FeeScheduleView create(UUID classLevelId, UUID termId, List<FeeItemSpec> items) {
        feeScheduleRepository
                .findByClassLevelIdAndTermIdAndArchivedAtIsNull(classLevelId, termId)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException(
                                    "BR-FI-001", "A fee schedule already exists for this class level and term");
                        });
        if (items.isEmpty()) {
            throw new BusinessRuleViolationException("BR-FI-001", "A fee schedule needs at least one fee item");
        }

        FeeSchedule schedule = feeScheduleRepository.save(new FeeSchedule(classLevelId, termId));
        List<FeeItemView> itemViews =
                items.stream()
                        .map(spec -> FeeItemView.from(feeItemRepository.save(new FeeItem(schedule, spec.name(), spec.amount(), spec.mandatory()))))
                        .toList();

        return FeeScheduleView.from(schedule, itemViews);
    }

    @Audited(action = "FEE_SCHEDULE_APPROVED", entityType = "FeeSchedule")
    @Transactional
    public FeeScheduleView approve(UUID feeScheduleId) {
        FeeSchedule schedule = getSchedule(feeScheduleId);
        schedule.approve();
        return get(feeScheduleId);
    }

    @Transactional(readOnly = true)
    public FeeScheduleView get(UUID feeScheduleId) {
        FeeSchedule schedule = getSchedule(feeScheduleId);
        List<FeeItemView> items =
                feeItemRepository.findByFeeScheduleIdAndArchivedAtIsNull(feeScheduleId).stream().map(FeeItemView::from).toList();
        return FeeScheduleView.from(schedule, items);
    }

    /** Used internally by the billing run — throws if no APPROVED schedule exists for this (level, term). */
    @Transactional(readOnly = true)
    public FeeScheduleView getApprovedForBilling(UUID classLevelId, UUID termId) {
        FeeSchedule schedule = feeScheduleRepository
                .findByClassLevelIdAndTermIdAndArchivedAtIsNull(classLevelId, termId)
                .orElseThrow(() -> new NotFoundException("No fee schedule defined for this class level and term"));
        if (schedule.getStatus() != FeeScheduleStatus.APPROVED) {
            throw new BusinessRuleViolationException("BR-FI-001", "The fee schedule must be approved by the Head before billing");
        }
        List<FeeItemView> items =
                feeItemRepository.findByFeeScheduleIdAndArchivedAtIsNull(schedule.getId()).stream().map(FeeItemView::from).toList();
        return FeeScheduleView.from(schedule, items);
    }

    private FeeSchedule getSchedule(UUID feeScheduleId) {
        return feeScheduleRepository
                .findById(feeScheduleId)
                .orElseThrow(() -> new NotFoundException("No such fee schedule: " + feeScheduleId));
    }
}
