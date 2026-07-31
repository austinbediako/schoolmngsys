package com.drakalabs.schoolmngsys.analytics.service;

import java.math.BigDecimal;

public record FinanceAnalyticsView(
        BigDecimal totalInvoicedAmount,
        BigDecimal totalCollectedAmount,
        BigDecimal totalOutstandingArrears,
        BigDecimal collectionPercentage
) {
}
