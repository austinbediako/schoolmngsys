package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Payment;
import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentView(
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
        List<PaymentAllocationView> allocations) {

    public static PaymentView from(Payment payment, List<PaymentAllocationView> allocations) {
        return new PaymentView(
                payment.getId(),
                payment.getEnrollmentId(),
                payment.getAmount(),
                payment.getChannel(),
                payment.getReference(),
                payment.getReceiptNumber(),
                payment.isReversed(),
                payment.getReversalOfPaymentId(),
                payment.getReversalReason(),
                payment.getAllocationOverrideReason(),
                allocations);
    }
}
