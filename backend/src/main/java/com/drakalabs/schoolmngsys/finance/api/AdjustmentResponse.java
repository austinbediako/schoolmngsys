package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.AdjustmentStatus;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentType;
import com.drakalabs.schoolmngsys.finance.service.AdjustmentView;
import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentResponse(
        UUID id, UUID invoiceId, AdjustmentType type, BigDecimal amount, String reason, AdjustmentStatus status, UUID invoiceLineId) {

    public static AdjustmentResponse from(AdjustmentView view) {
        return new AdjustmentResponse(
                view.id(), view.invoiceId(), view.type(), view.amount(), view.reason(), view.status(), view.invoiceLineId());
    }
}
