package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import com.drakalabs.schoolmngsys.finance.domain.Payment;
import com.drakalabs.schoolmngsys.finance.domain.PaymentAllocation;
import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceRepository;
import com.drakalabs.schoolmngsys.finance.repository.PaymentAllocationRepository;
import com.drakalabs.schoolmngsys.finance.repository.PaymentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BR-FI-002/A-09: a payment is allocated to the enrollment's open invoices oldest-first, part
 * payments included, until the payment is exhausted or every open invoice is settled; any
 * remainder is left unallocated as a standing credit rather than forced onto a fully-paid invoice.
 * An Accountant may target a specific invoice out of that order, but only with a recorded reason
 * (BR-FI-002 override). BR-FI-003: corrections are exclusively a reversal payment (negative
 * amount, mirrored negative allocations) — the original row is never edited, only flagged reversed.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLedgerService invoiceLedgerService;
    private final ReceiptNumberGenerator receiptNumberGenerator;
    private final com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher domainEventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            InvoiceRepository invoiceRepository,
            InvoiceLedgerService invoiceLedgerService,
            ReceiptNumberGenerator receiptNumberGenerator,
            com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher domainEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLedgerService = invoiceLedgerService;
        this.receiptNumberGenerator = receiptNumberGenerator;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Audited(action = "PAYMENT_RECORDED", entityType = "Payment")
    @Transactional
    public PaymentView recordPayment(
            UUID enrollmentId, BigDecimal amount, PaymentChannel channel, String reference, UUID targetInvoiceId, String overrideReason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("BR-FI-002", "A payment amount must be positive");
        }

        List<Invoice> targets;
        if (targetInvoiceId != null) {
            if (overrideReason == null || overrideReason.isBlank()) {
                throw new BusinessRuleViolationException(
                        "BR-FI-002", "Allocating to a specific invoice out of oldest-first order requires an override reason");
            }
            Invoice target = invoiceRepository
                    .findById(targetInvoiceId)
                    .orElseThrow(() -> new NotFoundException("No such invoice: " + targetInvoiceId));
            targets = List.of(target);
        } else {
            targets = invoiceRepository.findByEnrollmentIdAndStatusNotAndArchivedAtIsNullOrderByIssuedAtAsc(
                    enrollmentId, InvoiceStatus.PAID);
        }

        Payment payment = paymentRepository.save(
                Payment.original(enrollmentId, amount, channel, reference, receiptNumberGenerator.generate(), overrideReason));

        List<PaymentAllocationView> allocationViews = new ArrayList<>();
        BigDecimal remaining = amount;
        for (Invoice invoice : targets) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal balance = invoiceLedgerService.balance(invoice);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal applied = remaining.min(balance);
            PaymentAllocation allocation = paymentAllocationRepository.save(new PaymentAllocation(payment, invoice, applied));
            allocationViews.add(PaymentAllocationView.from(allocation));
            invoiceLedgerService.refreshStatus(invoice);
            remaining = remaining.subtract(applied);
        }

        PaymentView view = PaymentView.from(payment, allocationViews);
        domainEventPublisher.publish(new com.drakalabs.schoolmngsys.finance.domain.PaymentReceived(
                payment.getId(), enrollmentId, amount, payment.getReceiptNumber()));

        return view;
    }

    @Audited(action = "PAYMENT_REVERSED", entityType = "Payment")
    @Transactional
    public PaymentView reversePayment(UUID paymentId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("BR-FI-003", "A reversal requires a reason");
        }
        Payment original = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("No such payment: " + paymentId));
        if (original.isReversed()) {
            throw new BusinessRuleViolationException("BR-FI-003", "This payment has already been reversed");
        }
        if (original.isReversal()) {
            throw new BusinessRuleViolationException("BR-FI-003", "A reversal payment cannot itself be reversed");
        }

        Payment reversal = paymentRepository.save(Payment.reversalOf(original, receiptNumberGenerator.generate(), reason));

        List<PaymentAllocationView> allocationViews = new ArrayList<>();
        for (PaymentAllocation allocation : paymentAllocationRepository.findByPaymentIdAndArchivedAtIsNull(paymentId)) {
            PaymentAllocation reversalAllocation = paymentAllocationRepository.save(
                    new PaymentAllocation(reversal, allocation.getInvoice(), allocation.getAmount().negate()));
            allocationViews.add(PaymentAllocationView.from(reversalAllocation));
            invoiceLedgerService.refreshStatus(allocation.getInvoice());
        }

        original.markReversed();
        paymentRepository.save(original);

        return PaymentView.from(reversal, allocationViews);
    }

    @Transactional(readOnly = true)
    public PaymentView get(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("No such payment: " + paymentId));
        return PaymentView.from(payment, allocationsOf(paymentId));
    }

    @Transactional(readOnly = true)
    public List<PaymentView> history(UUID enrollmentId) {
        return paymentRepository.findByEnrollmentIdOrderByCreatedAtDesc(enrollmentId).stream()
                .map(payment -> PaymentView.from(payment, allocationsOf(payment.getId())))
                .toList();
    }

    private List<PaymentAllocationView> allocationsOf(UUID paymentId) {
        return paymentAllocationRepository.findByPaymentIdAndArchivedAtIsNull(paymentId).stream()
                .map(PaymentAllocationView::from)
                .toList();
    }
}
