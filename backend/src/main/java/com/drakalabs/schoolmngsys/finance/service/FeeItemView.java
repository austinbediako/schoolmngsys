package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.FeeItem;
import java.math.BigDecimal;
import java.util.UUID;

public record FeeItemView(UUID id, String name, BigDecimal amount, boolean mandatory) {

    public static FeeItemView from(FeeItem item) {
        return new FeeItemView(item.getId(), item.getName(), item.getAmount(), item.isMandatory());
    }
}
