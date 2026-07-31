package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.FeeSchedule;
import com.drakalabs.schoolmngsys.finance.domain.FeeScheduleStatus;
import java.util.List;
import java.util.UUID;

public record FeeScheduleView(UUID id, UUID classLevelId, UUID termId, FeeScheduleStatus status, List<FeeItemView> items) {

    public static FeeScheduleView from(FeeSchedule schedule, List<FeeItemView> items) {
        return new FeeScheduleView(schedule.getId(), schedule.getClassLevelId(), schedule.getTermId(), schedule.getStatus(), items);
    }
}
