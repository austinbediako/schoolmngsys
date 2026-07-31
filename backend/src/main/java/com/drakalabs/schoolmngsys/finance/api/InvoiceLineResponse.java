package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.InvoiceLineSourceType;
import com.drakalabs.schoolmngsys.finance.service.InvoiceLineView;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLineResponse(UUID id, String description, BigDecimal amount, InvoiceLineSourceType sourceType) {

    public static InvoiceLineResponse from(InvoiceLineView view) {
        return new InvoiceLineResponse(view.id(), view.description(), view.amount(), view.sourceType());
    }
}
