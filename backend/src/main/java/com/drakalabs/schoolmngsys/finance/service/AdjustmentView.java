package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Adjustment;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentStatus;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentType;
import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentView(
        UUID id, UUID invoiceId, AdjustmentType type, BigDecimal amount, String reason, AdjustmentStatus status, UUID invoiceLineId) {

    public static AdjustmentView from(Adjustment adjustment) {
        return new AdjustmentView(
                adjustment.getId(),
                adjustment.getInvoice().getId(),
                adjustment.getType(),
                adjustment.getAmount(),
                adjustment.getReason(),
                adjustment.getStatus(),
                adjustment.getInvoiceLineId());
    }
}
