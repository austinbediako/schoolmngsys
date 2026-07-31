package com.drakalabs.schoolmngsys.finance.api;

import jakarta.validation.constraints.NotBlank;

public record ReversePaymentRequest(@NotBlank String reason) {
}
