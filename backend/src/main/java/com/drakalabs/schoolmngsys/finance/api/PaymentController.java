package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.FinanceAccessGuard;
import com.drakalabs.schoolmngsys.finance.service.PaymentService;
import com.drakalabs.schoolmngsys.finance.service.PaymentView;
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
public class PaymentController {

    private final PaymentService paymentService;
    private final FinanceAccessGuard financeAccessGuard;

    public PaymentController(PaymentService paymentService, FinanceAccessGuard financeAccessGuard) {
        this.paymentService = paymentService;
        this.financeAccessGuard = financeAccessGuard;
    }

    @PostMapping("/api/v1/payments")
    @PreAuthorize("hasAuthority('PAYMENT_RECORD')")
    public PaymentResponse record(@RequestBody @Valid RecordPaymentRequest request) {
        return PaymentResponse.from(paymentService.recordPayment(
                request.enrollmentId(),
                request.amount(),
                request.channel(),
                request.reference(),
                request.targetInvoiceId(),
                request.overrideReason()));
    }

    @PostMapping("/api/v1/payments/{id}/reverse")
    @PreAuthorize("hasAuthority('PAYMENT_REVERSE')")
    public PaymentResponse reverse(@PathVariable UUID id, @RequestBody @Valid ReversePaymentRequest request) {
        return PaymentResponse.from(paymentService.reversePayment(id, request.reason()));
    }

    @GetMapping("/api/v1/payments/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public PaymentResponse get(@PathVariable UUID id) {
        PaymentView view = paymentService.get(id);
        financeAccessGuard.assertCanViewEnrollment(view.enrollmentId());
        return PaymentResponse.from(view);
    }

    @GetMapping("/api/v1/enrollments/{enrollmentId}/payments")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public List<PaymentResponse> history(@PathVariable UUID enrollmentId) {
        financeAccessGuard.assertCanViewEnrollment(enrollmentId);
        return paymentService.history(enrollmentId).stream().map(PaymentResponse::from).toList();
    }
}
