package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.PaymentAllocation;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAllocationView(UUID id, UUID invoiceId, BigDecimal amount) {

    public static PaymentAllocationView from(PaymentAllocation allocation) {
        return new PaymentAllocationView(allocation.getId(), allocation.getInvoice().getId(), allocation.getAmount());
    }
}
