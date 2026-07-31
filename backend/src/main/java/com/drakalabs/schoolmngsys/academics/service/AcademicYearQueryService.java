package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.academics.repository.TermRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicYearQueryService {

    private final AcademicYearRepository academicYearRepository;
    private final TermRepository termRepository;

    public AcademicYearQueryService(AcademicYearRepository academicYearRepository, TermRepository termRepository) {
        this.academicYearRepository = academicYearRepository;
        this.termRepository = termRepository;
    }

    @Transactional(readOnly = true)
    public Page<AcademicYearView> list(Pageable pageable) {
        return academicYearRepository.findAll(pageable).map(AcademicYearView::from);
    }

    @Transactional(readOnly = true)
    public AcademicYearView get(UUID academicYearId) {
        return academicYearRepository
                .findById(academicYearId)
                .map(AcademicYearView::from)
                .orElseThrow(() -> new NotFoundException("No such academic year: " + academicYearId));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<AcademicYearView> findActiveYear() {
        return academicYearRepository.findByStatusAndArchivedAtIsNull(com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus.ACTIVE)
                .map(AcademicYearView::from);
    }

    @Transactional(readOnly = true)
    public TermView getTerm(UUID termId) {
        return termRepository.findById(termId).map(TermView::from).orElseThrow(() -> new NotFoundException("No such term: " + termId));
    }

    @Transactional(readOnly = true)
    public List<TermView> listTerms(UUID academicYearId) {
        if (!academicYearRepository.existsById(academicYearId)) {
            throw new NotFoundException("No such academic year: " + academicYearId);
        }
        return termRepository.findByAcademicYearIdAndArchivedAtIsNullOrderByTermNumber(academicYearId).stream()
                .map(TermView::from)
                .toList();
    }
}
