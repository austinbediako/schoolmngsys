package com.drakalabs.schoolmngsys.finance.api;

import com.drakalabs.schoolmngsys.finance.service.BillingRunService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {

    private final BillingRunService billingRunService;

    public BillingController(BillingRunService billingRunService) {
        this.billingRunService = billingRunService;
    }

    @PostMapping("/api/v1/billing-runs")
    @PreAuthorize("hasAuthority('BILLING_RUN_EXECUTE')")
    public List<InvoiceResponse> run(@RequestParam UUID classLevelId, @RequestParam UUID termId) {
        return billingRunService.runBilling(classLevelId, termId).stream().map(InvoiceResponse::from).toList();
    }
}
