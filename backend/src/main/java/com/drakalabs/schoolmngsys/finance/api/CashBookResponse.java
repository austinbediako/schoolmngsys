package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.CollectionSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CashBookResponse(Instant from, Instant to, BigDecimal totalCollected, List<PaymentResponse> payments) {

    public static CashBookResponse from(CollectionSummary summary) {
        return new CashBookResponse(
                summary.from(), summary.to(), summary.totalCollected(), summary.payments().stream().map(PaymentResponse::from).toList());
    }
}
