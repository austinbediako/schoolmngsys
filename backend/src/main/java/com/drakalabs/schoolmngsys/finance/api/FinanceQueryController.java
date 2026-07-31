package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.FinanceAccessGuard;
import com.drakalabs.schoolmngsys.finance.service.FinanceQueryService;
import com.drakalabs.schoolmngsys.finance.service.InvoiceView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinanceQueryController {

    private final FinanceQueryService financeQueryService;
    private final FinanceAccessGuard financeAccessGuard;

    public FinanceQueryController(FinanceQueryService financeQueryService, FinanceAccessGuard financeAccessGuard) {
        this.financeQueryService = financeQueryService;
        this.financeAccessGuard = financeAccessGuard;
    }

    @GetMapping("/api/v1/invoices/{id}")
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        InvoiceView view = financeQueryService.getInvoice(id);
        financeAccessGuard.assertCanViewEnrollment(view.enrollmentId());
        return InvoiceResponse.from(view);
    }

    @GetMapping("/api/v1/enrollments/{enrollmentId}/invoices")
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public List<InvoiceResponse> invoiceHistory(@PathVariable UUID enrollmentId) {
        financeAccessGuard.assertCanViewEnrollment(enrollmentId);
        return financeQueryService.invoiceHistory(enrollmentId).stream().map(InvoiceResponse::from).toList();
    }

    @GetMapping("/api/v1/class-levels/{classLevelId}/arrears")
    @PreAuthorize("hasAuthority('FINANCE_REPORT_VIEW')")
    public List<ArrearsEntryResponse> arrears(@PathVariable UUID classLevelId, @RequestParam UUID academicYearId) {
        return financeQueryService.arrears(classLevelId, academicYearId).stream().map(ArrearsEntryResponse::from).toList();
    }

    @GetMapping("/api/v1/finance/cash-book")
    @PreAuthorize("hasAuthority('FINANCE_REPORT_VIEW')")
    public CashBookResponse cashBook(@RequestParam Instant from, @RequestParam Instant to) {
        return CashBookResponse.from(financeQueryService.cashBook(from, to));
    }

    @GetMapping("/api/v1/finance/cash-book/export")
    @PreAuthorize("hasAuthority('FINANCE_REPORT_VIEW') or hasAuthority('EXPORT_EXECUTE')")
    public org.springframework.http.ResponseEntity<String> exportCashBookCsv(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String csvData = financeQueryService.exportCashBookCsv(from, to);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cash-book-summary.csv\"")
                .body(csvData);
    }
}
