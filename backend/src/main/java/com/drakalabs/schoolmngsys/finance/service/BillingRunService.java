package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.finance.domain.Invoice;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceLine;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceLineSourceType;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceLineRepository;
import com.drakalabs.schoolmngsys.finance.repository.InvoiceRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-FIN-02: creates an invoice for every active enrollment across every class at a level, for a
 * term, from that (level, term)'s APPROVED fee schedule. Idempotent: an enrollment already billed
 * for this term is skipped, not re-billed, so a re-run after adding a class mid-run is safe.
 * Arrears (BR-FI-005) are not duplicated onto the new invoice — see {@link InvoiceLedgerService}
 * and {@link PaymentService}: an older unpaid invoice simply stays open and gets paid down first.
 */
@Service
public class BillingRunService {

    private final FeeScheduleService feeScheduleService;
    private final AcademicYearQueryService academicYearQueryService;
    private final ClassQueryService classQueryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final InvoiceLedgerService invoiceLedgerService;
    private final com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher domainEventPublisher;

    public BillingRunService(
            FeeScheduleService feeScheduleService,
            AcademicYearQueryService academicYearQueryService,
            ClassQueryService classQueryService,
            EnrollmentQueryService enrollmentQueryService,
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            InvoiceLedgerService invoiceLedgerService,
            com.drakalabs.schoolmngsys.shared.event.DomainEventPublisher domainEventPublisher) {
        this.feeScheduleService = feeScheduleService;
        this.academicYearQueryService = academicYearQueryService;
        this.classQueryService = classQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.invoiceLedgerService = invoiceLedgerService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Audited(action = "BILLING_RUN_EXECUTED", entityType = "Invoice")
    @Transactional
    public List<InvoiceView> runBilling(UUID classLevelId, UUID termId) {
        FeeScheduleView schedule = feeScheduleService.getApprovedForBilling(classLevelId, termId);
        UUID academicYearId = academicYearQueryService.getTerm(termId).academicYearId();
        List<ClassView> classes = classQueryService.listByLevel(classLevelId);

        List<InvoiceView> created = new ArrayList<>();
        for (ClassView schoolClass : classes) {
            for (EnrollmentView enrollment : enrollmentQueryService.roster(schoolClass.id(), academicYearId)) {
                if (invoiceRepository.findByEnrollmentIdAndTermIdAndArchivedAtIsNull(enrollment.id(), termId).isPresent()) {
                    continue;
                }
                Invoice invoice = invoiceRepository.save(new Invoice(enrollment.id(), termId));
                for (FeeItemView item : schedule.items()) {
                    invoiceLineRepository.save(new InvoiceLine(invoice, item.name(), item.amount(), InvoiceLineSourceType.FEE_ITEM));
                }
                InvoiceView view = toView(invoice);
                created.add(view);
                domainEventPublisher.publish(new com.drakalabs.schoolmngsys.finance.domain.InvoiceIssued(
                        invoice.getId(), enrollment.id(), termId, view.totalAmount()));
            }
        }
        return created;
    }

    private InvoiceView toView(Invoice invoice) {
        List<InvoiceLineView> lines =
                invoiceLineRepository.findByInvoiceIdAndArchivedAtIsNull(invoice.getId()).stream().map(InvoiceLineView::from).toList();
        return InvoiceView.from(invoice, lines, invoiceLedgerService.totalAmount(invoice), invoiceLedgerService.balance(invoice));
    }
}
