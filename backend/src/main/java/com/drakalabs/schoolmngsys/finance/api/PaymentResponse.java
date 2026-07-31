package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import com.drakalabs.schoolmngsys.finance.service.PaymentView;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID enrollmentId,
        BigDecimal amount,
        PaymentChannel channel,
        String reference,
        String receiptNumber,
        boolean reversed,
        UUID reversalOfPaymentId,
        String reversalReason,
        String allocationOverrideReason,
        List<PaymentAllocationResponse> allocations) {

    public static PaymentResponse from(PaymentView view) {
        return new PaymentResponse(
                view.id(),
                view.enrollmentId(),
                view.amount(),
                view.channel(),
                view.reference(),
                view.receiptNumber(),
                view.reversed(),
                view.reversalOfPaymentId(),
                view.reversalReason(),
                view.allocationOverrideReason(),
                view.allocations().stream().map(PaymentAllocationResponse::from).toList());
    }
}
