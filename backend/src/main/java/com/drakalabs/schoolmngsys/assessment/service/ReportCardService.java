package com.drakalabs.schoolmngsys.assessment.service;

import com.drakalabs.schoolmngsys.assessment.domain.ReportCard;
import com.drakalabs.schoolmngsys.assessment.domain.ResultStatus;
import com.drakalabs.schoolmngsys.assessment.domain.TermResult;
import com.drakalabs.schoolmngsys.assessment.repository.ReportCardRepository;
import com.drakalabs.schoolmngsys.assessment.repository.TermResultRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-RES-05: report-card data per (Student, Term). BR-AA-004: class position by average of subject weighted totals. */
@Service
public class ReportCardService {

    private final ReportCardRepository reportCardRepository;
    private final TermResultRepository termResultRepository;

    public ReportCardService(ReportCardRepository reportCardRepository, TermResultRepository termResultRepository) {
        this.reportCardRepository = reportCardRepository;
        this.termResultRepository = termResultRepository;
    }

    /** Called once a class's subjects are all PUBLISHED (WF-04 step H) — ranks the whole class at once. */
    @Audited(action = "REPORT_CARDS_PUBLISHED", entityType = "ReportCard")
    @Transactional
    public List<ReportCardView> publishForClass(List<UUID> enrollmentIds, UUID termId) {
        record Average(UUID enrollmentId, BigDecimal average) {
        }

        List<Average> averages =
                enrollmentIds.stream()
                        .map(
                                enrollmentId -> {
                                    List<TermResult> results = termResultRepository.findByEnrollmentIdAndTermIdAndArchivedAtIsNull(
                                            enrollmentId, termId);
                                    BigDecimal average = results.isEmpty()
                                            ? BigDecimal.ZERO
                                            : results.stream()
                                                    .map(TermResult::getWeightedTotal)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                    .divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
                                    return new Average(enrollmentId, average);
                                })
                        .sorted(Comparator.comparing(Average::average).reversed())
                        .collect(Collectors.toList());

        int position = 0;
        int rank = 0;
        BigDecimal previous = null;
        for (Average average : averages) {
            position++;
            if (previous == null || average.average().compareTo(previous) != 0) {
                rank = position;
            }
            ReportCard card = reportCardRepository
                    .findByEnrollmentIdAndTermIdAndArchivedAtIsNull(average.enrollmentId(), termId)
                    .orElseGet(() -> new ReportCard(average.enrollmentId(), termId));
            card.assignClassPosition(rank);
            card.publish();
            reportCardRepository.save(card);
            previous = average.average();
        }

        return enrollmentIds.stream()
                .map(id -> reportCardRepository.findByEnrollmentIdAndTermIdAndArchivedAtIsNull(id, termId).orElseThrow())
                .map(ReportCardView::from)
                .toList();
    }

    @Audited(action = "REPORT_CARD_REMARKS_UPDATED", entityType = "ReportCard")
    @Transactional
    public ReportCardView updateRemarks(UUID enrollmentId, UUID termId, String conductRemark, String interestRemark, String headRemark) {
        ReportCard card = reportCardRepository
                .findByEnrollmentIdAndTermIdAndArchivedAtIsNull(enrollmentId, termId)
                .orElseGet(() -> new ReportCard(enrollmentId, termId));
        card.updateRemarks(conductRemark, interestRemark, headRemark);
        return ReportCardView.from(reportCardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public ReportCardView get(UUID enrollmentId, UUID termId) {
        return reportCardRepository
                .findByEnrollmentIdAndTermIdAndArchivedAtIsNull(enrollmentId, termId)
                .map(ReportCardView::from)
                .orElseThrow(() -> new NotFoundException("No report card for this enrollment/term yet"));
    }
}
