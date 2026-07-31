package com.drakalabs.schoolmngsys.finance.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FeeItemRequest(@NotBlank String name, @NotNull @DecimalMin(value = "0.01") BigDecimal amount, boolean mandatory) {
}
