package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull UUID enrollmentId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentChannel channel,
        String reference,
        UUID targetInvoiceId,
        String overrideReason) {
}
