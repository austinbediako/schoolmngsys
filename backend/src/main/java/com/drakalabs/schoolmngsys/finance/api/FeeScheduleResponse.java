package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.FeeScheduleStatus;
import com.drakalabs.schoolmngsys.finance.service.FeeScheduleView;
import java.util.List;
import java.util.UUID;

public record FeeScheduleResponse(UUID id, UUID classLevelId, UUID termId, FeeScheduleStatus status, List<FeeItemResponse> items) {

    public static FeeScheduleResponse from(FeeScheduleView view) {
        return new FeeScheduleResponse(
                view.id(), view.classLevelId(), view.termId(), view.status(), view.items().stream().map(FeeItemResponse::from).toList());
    }
}
