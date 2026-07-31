package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.FeeItemView;
import java.math.BigDecimal;
import java.util.UUID;

public record FeeItemResponse(UUID id, String name, BigDecimal amount, boolean mandatory) {

    public static FeeItemResponse from(FeeItemView view) {
        return new FeeItemResponse(view.id(), view.name(), view.amount(), view.mandatory());
    }
}
