package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.PaymentAllocationView;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAllocationResponse(UUID id, UUID invoiceId, BigDecimal amount) {

    public static PaymentAllocationResponse from(PaymentAllocationView view) {
        return new PaymentAllocationResponse(view.id(), view.invoiceId(), view.amount());
    }
}
