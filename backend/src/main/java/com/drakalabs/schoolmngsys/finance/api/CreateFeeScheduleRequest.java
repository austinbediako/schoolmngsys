package com.drakalabs.schoolmngsys.finance.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateFeeScheduleRequest(
        @NotNull UUID classLevelId, @NotNull UUID termId, @NotEmpty @Valid List<FeeItemRequest> items) {
}
