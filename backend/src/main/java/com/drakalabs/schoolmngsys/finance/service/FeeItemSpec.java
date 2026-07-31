package com.drakalabs.schoolmngsys.finance.service;

import java.math.BigDecimal;

public record FeeItemSpec(String name, BigDecimal amount, boolean mandatory) {
}
