package com.drakalabs.schoolmngsys.assessment.api;

import com.drakalabs.schoolmngsys.assessment.service.ReportCardService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportCardController {

    private final ReportCardService reportCardService;

    public ReportCardController(ReportCardService reportCardService) {
        this.reportCardService = reportCardService;
    }

    @GetMapping("/api/v1/enrollments/{enrollmentId}/report-card")
    @PreAuthorize("hasAuthority('REPORT_CARD_VIEW')")
    public ReportCardResponse get(@PathVariable UUID enrollmentId, @RequestParam UUID termId) {
        return ReportCardResponse.from(reportCardService.get(enrollmentId, termId));
    }

    /** WF-04 step G: class-teacher conduct/interest remarks, Head remark — gated the same as submitting results. */
    @PutMapping("/api/v1/enrollments/{enrollmentId}/report-card/remarks")
    @PreAuthorize("hasAuthority('RESULT_SUBMIT')")
    public ReportCardResponse updateRemarks(
            @PathVariable UUID enrollmentId, @RequestParam UUID termId, @RequestBody @Valid UpdateReportCardRemarksRequest request) {
        return ReportCardResponse.from(
                reportCardService.updateRemarks(enrollmentId, termId, request.conductRemark(), request.interestRemark(), request.headRemark()));
    }
}
