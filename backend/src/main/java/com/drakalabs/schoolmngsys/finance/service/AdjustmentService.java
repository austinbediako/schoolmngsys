package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.finance.domain.Adjustment;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentStatus;
import com.drakalabs.schoolmngsys.finance.domain.AdjustmentType;
import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceLine;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceLineSourceType;
import com.drakalabs.schoolmngsys.finance.repository.AdjustmentRepository;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceLineRepository;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BR-FI-004: an Accountant proposes a discount/scholarship/waiver against an invoice; it has no
 * financial effect until a Head approves it, at which point (and only then) it becomes a real
 * negative {@link InvoiceLine} and the invoice's balance/status are recomputed. A rejected or
 * still-PROPOSED adjustment never touches the ledger.
 */
@Service
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final InvoiceLedgerService invoiceLedgerService;

    public AdjustmentService(
            AdjustmentRepository adjustmentRepository,
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            InvoiceLedgerService invoiceLedgerService) {
        this.adjustmentRepository = adjustmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.invoiceLedgerService = invoiceLedgerService;
    }

    @Audited(action = "ADJUSTMENT_PROPOSED", entityType = "Adjustment")
    @Transactional
    public AdjustmentView propose(UUID invoiceId, AdjustmentType type, BigDecimal amount, String reason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("BR-FI-004", "An adjustment amount must be positive");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("BR-FI-004", "An adjustment requires a reason");
        }
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new NotFoundException("No such invoice: " + invoiceId));

        Adjustment adjustment = adjustmentRepository.save(new Adjustment(invoice, type, amount, reason));
        return AdjustmentView.from(adjustment);
    }

    @Audited(action = "ADJUSTMENT_APPROVED", entityType = "Adjustment")
    @Transactional
    public AdjustmentView approve(UUID adjustmentId) {
        Adjustment adjustment = getAdjustment(adjustmentId);
        if (adjustment.getStatus() != AdjustmentStatus.PROPOSED) {
            throw new BusinessRuleViolationException("BR-FI-004", "Only a PROPOSED adjustment can be approved");
        }

        InvoiceLine line = invoiceLineRepository.save(new InvoiceLine(
                adjustment.getInvoice(),
                adjustment.getType() + ": " + adjustment.getReason(),
                adjustment.getAmount().negate(),
                InvoiceLineSourceType.ADJUSTMENT));
        adjustment.approve(line.getId());
        adjustmentRepository.save(adjustment);

        invoiceLedgerService.refreshStatus(adjustment.getInvoice());

        return AdjustmentView.from(adjustment);
    }

    @Audited(action = "ADJUSTMENT_REJECTED", entityType = "Adjustment")
    @Transactional
    public AdjustmentView reject(UUID adjustmentId) {
        Adjustment adjustment = getAdjustment(adjustmentId);
        if (adjustment.getStatus() != AdjustmentStatus.PROPOSED) {
            throw new BusinessRuleViolationException("BR-FI-004", "Only a PROPOSED adjustment can be rejected");
        }
        adjustment.reject();
        adjustmentRepository.save(adjustment);
        return AdjustmentView.from(adjustment);
    }

    @Transactional(readOnly = true)
    public List<AdjustmentView> forInvoice(UUID invoiceId) {
        return adjustmentRepository.findByInvoiceIdAndArchivedAtIsNull(invoiceId).stream().map(AdjustmentView::from).toList();
    }

    private Adjustment getAdjustment(UUID adjustmentId) {
        return adjustmentRepository.findById(adjustmentId).orElseThrow(() -> new NotFoundException("No such adjustment: " + adjustmentId));
    }
}
