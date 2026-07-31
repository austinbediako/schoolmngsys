package com.drakalabs.schoolmngsys.finance.service;

import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.people.service.GuardianWardResolutionService;
import com.drakalabs.schoolmngsys.shared.security.AuthenticatedAccount;
import com.drakalabs.schoolmngsys.shared.security.CurrentAccountProvider;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scope filter for INVOICE_VIEW/PAYMENT_VIEW (docs/11 §3, BR-FI-006): staff callers see every
 * enrollment; a GUARDIAN caller may only reach an enrollment belonging to one of their own wards
 * (via {@link GuardianWardResolutionService}, the named authorization scope source per docs/08 §4).
 * Holding the permission string is necessary but not sufficient — this closes the gap where
 * {@code GUARDIAN} holds INVOICE_VIEW/PAYMENT_VIEW broadly but must not see other families' records.
 */
@Component
public class FinanceAccessGuard {

    private final CurrentAccountProvider currentAccountProvider;
    private final GuardianWardResolutionService guardianWardResolutionService;
    private final EnrollmentQueryService enrollmentQueryService;

    public FinanceAccessGuard(
            CurrentAccountProvider currentAccountProvider,
            GuardianWardResolutionService guardianWardResolutionService,
            EnrollmentQueryService enrollmentQueryService) {
        this.currentAccountProvider = currentAccountProvider;
        this.guardianWardResolutionService = guardianWardResolutionService;
        this.enrollmentQueryService = enrollmentQueryService;
    }

    @Transactional(readOnly = true)
    public void assertCanViewEnrollment(UUID enrollmentId) {
        AuthenticatedAccount account =
                currentAccountProvider.current().orElseThrow(() -> new AccessDeniedException("No authenticated account"));

        if (account.personType() != PersonType.GUARDIAN) {
            return; // staff callers are scoped by permission alone (docs/11 §3)
        }

        UUID studentId = enrollmentQueryService.get(enrollmentId).studentId();
        if (!guardianWardResolutionService.isWardOf(account.personId(), studentId)) {
            throw new AccessDeniedException("Not authorized to view financial records for this enrollment");
        }
    }
}
