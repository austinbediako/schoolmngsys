package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.AdjustmentService;
import com.drakalabs.schoolmngsys.finance.service.FinanceAccessGuard;
import com.drakalabs.schoolmngsys.finance.service.FinanceQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdjustmentController {

    private final AdjustmentService adjustmentService;
    private final FinanceQueryService financeQueryService;
    private final FinanceAccessGuard financeAccessGuard;

    public AdjustmentController(
            AdjustmentService adjustmentService, FinanceQueryService financeQueryService, FinanceAccessGuard financeAccessGuard) {
        this.adjustmentService = adjustmentService;
        this.financeQueryService = financeQueryService;
        this.financeAccessGuard = financeAccessGuard;
    }

    @PostMapping("/api/v1/invoices/{invoiceId}/adjustments")
    @PreAuthorize("hasAuthority('ADJUSTMENT_PROPOSE')")
    public AdjustmentResponse propose(@PathVariable UUID invoiceId, @RequestBody @Valid ProposeAdjustmentRequest request) {
        return AdjustmentResponse.from(adjustmentService.propose(invoiceId, request.type(), request.amount(), request.reason()));
    }

    @PostMapping("/api/v1/adjustments/{id}/approve")
    @PreAuthorize("hasAuthority('ADJUSTMENT_APPROVE')")
    public AdjustmentResponse approve(@PathVariable UUID id) {
        return AdjustmentResponse.from(adjustmentService.approve(id));
    }

    @PostMapping("/api/v1/adjustments/{id}/reject")
    @PreAuthorize("hasAuthority('ADJUSTMENT_APPROVE')")
    public AdjustmentResponse reject(@PathVariable UUID id) {
        return AdjustmentResponse.from(adjustmentService.reject(id));
    }

    @GetMapping("/api/v1/invoices/{invoiceId}/adjustments")
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public List<AdjustmentResponse> forInvoice(@PathVariable UUID invoiceId) {
        financeAccessGuard.assertCanViewEnrollment(financeQueryService.getInvoice(invoiceId).enrollmentId());
        return adjustmentService.forInvoice(invoiceId).stream().map(AdjustmentResponse::from).toList();
    }
}
