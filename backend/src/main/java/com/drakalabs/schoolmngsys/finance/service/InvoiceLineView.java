package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.InvoiceLine;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceLineSourceType;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineView(UUID id, String description, BigDecimal amount, InvoiceLineSourceType sourceType) {

    public static InvoiceLineView from(InvoiceLine line) {
        return new InvoiceLineView(line.getId(), line.getDescription(), line.getAmount(), line.getSourceType());
    }
}
