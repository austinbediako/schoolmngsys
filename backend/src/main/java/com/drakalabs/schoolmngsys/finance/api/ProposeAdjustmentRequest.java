package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.domain.AdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProposeAdjustmentRequest(
        @NotNull AdjustmentType type, @NotNull @DecimalMin(value = "0.01") BigDecimal amount, @NotBlank String reason) {
}
