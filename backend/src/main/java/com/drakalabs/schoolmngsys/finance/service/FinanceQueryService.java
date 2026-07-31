package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceStatus;
import com.drakalabs.schoolmngsys.finance.domain.Payment;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceLineRepository;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceRepository;
import com.drakalabs.schoolmngsys.finance.repository.PaymentRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-FIN-06 read-side reports: per-invoice balances, arrears aging by (class level, term), and the daily cash book. */
@Service
public class FinanceQueryService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceLedgerService invoiceLedgerService;
    private final ClassQueryService classQueryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final Clock clock;

    public FinanceQueryService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            PaymentRepository paymentRepository,
            InvoiceLedgerService invoiceLedgerService,
            ClassQueryService classQueryService,
            EnrollmentQueryService enrollmentQueryService,
            Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceLedgerService = invoiceLedgerService;
        this.classQueryService = classQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public InvoiceView getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new NotFoundException("No such invoice: " + invoiceId));
        return toView(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceView> invoiceHistory(UUID enrollmentId) {
        return invoiceRepository.findByEnrollmentIdAndArchivedAtIsNullOrderByIssuedAtDesc(enrollmentId).stream()
                .map(this::toView)
                .toList();
    }

    /** Every open invoice for enrollments in classes at {@code classLevelId} for {@code academicYearId}, with its age. */
    @Transactional(readOnly = true)
    public List<ArrearsEntry> arrears(UUID classLevelId, UUID academicYearId) {
        Instant now = clock.instant();
        List<ArrearsEntry> entries = new ArrayList<>();
        for (ClassView schoolClass : classQueryService.listByLevel(classLevelId)) {
            for (EnrollmentView enrollment : enrollmentQueryService.roster(schoolClass.id(), academicYearId)) {
                for (Invoice invoice : invoiceRepository.findByEnrollmentIdAndArchivedAtIsNullOrderByIssuedAtDesc(enrollment.id())) {
                    if (invoice.getStatus() == InvoiceStatus.PAID) {
                        continue;
                    }
                    BigDecimal balance = invoiceLedgerService.balance(invoice);
                    if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    long daysOutstanding = Duration.between(invoice.getIssuedAt(), now).toDays();
                    entries.add(new ArrearsEntry(invoice.getId(), enrollment.id(), invoice.getIssuedAt(), balance, daysOutstanding));
                }
            }
        }
        return entries;
    }

    /** The daily cash book (FR-FIN-06): every payment posted in [from, to), reversals included (their negative amount nets the book out). */
    @Transactional(readOnly = true)
    public CollectionSummary cashBook(Instant from, Instant to) {
        List<Payment> payments = paymentRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(from, to);
        BigDecimal total = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PaymentView> views = payments.stream().map(payment -> PaymentView.from(payment, List.of())).toList();
        return new CollectionSummary(from, to, total, views);
    }

    @Transactional(readOnly = true)
    public String exportCashBookCsv(Instant from, Instant to) {
        CollectionSummary summary = cashBook(from, to);
        StringBuilder csv = new StringBuilder();
        csv.append("Receipt Number,Enrollment ID,Channel,Reference,Amount (GHS),Reversed\n");
        for (PaymentView p : summary.payments()) {
            csv.append(escapeCsv(p.receiptNumber())).append(",")
               .append(escapeCsv(p.enrollmentId() != null ? p.enrollmentId().toString() : "")).append(",")
               .append(escapeCsv(p.channel() != null ? p.channel().name() : "")).append(",")
               .append(escapeCsv(p.reference() != null ? p.reference() : "")).append(",")
               .append(p.amount()).append(",")
               .append(p.reversed()).append("\n");
        }
        csv.append("TOTAL,,,,").append(summary.totalCollected()).append(",\n");
        return csv.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private InvoiceView toView(Invoice invoice) {
        List<InvoiceLineView> lines =
                invoiceLineRepository.findByInvoiceIdAndArchivedAtIsNull(invoice.getId()).stream().map(InvoiceLineView::from).toList();
        return InvoiceView.from(invoice, lines, invoiceLedgerService.totalAmount(invoice), invoiceLedgerService.balance(invoice));
    }
}
