package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.repository.ClassRepository;
import com.drakalabs.schoolmngsys.academics.repository.ClassSubjectOfferingRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassQueryService {

    private final ClassRepository classRepository;
    private final ClassSubjectOfferingRepository classSubjectOfferingRepository;
    private final com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository classLevelRepository;

    public ClassQueryService(
            ClassRepository classRepository,
            ClassSubjectOfferingRepository classSubjectOfferingRepository,
            com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository classLevelRepository) {
        this.classRepository = classRepository;
        this.classSubjectOfferingRepository = classSubjectOfferingRepository;
        this.classLevelRepository = classLevelRepository;
    }

    @Transactional(readOnly = true)
    public ClassLevelView getClassLevel(UUID classLevelId) {
        return classLevelRepository.findById(classLevelId)
                .map(ClassLevelView::from)
                .orElseThrow(() -> new NotFoundException("No such class level: " + classLevelId));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ClassLevelView> getClassLevelBySequence(int sequence) {
        return classLevelRepository.findBySequenceAndArchivedAtIsNull(sequence).map(ClassLevelView::from);
    }

    @Transactional(readOnly = true)
    public List<ClassLevelView> listClassLevels() {
        return classLevelRepository.findAllByArchivedAtIsNullOrderBySequenceAsc().stream().map(ClassLevelView::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ClassView> list(Pageable pageable) {
        return classRepository.findAll(pageable).map(ClassView::from);
    }

    @Transactional(readOnly = true)
    public ClassView get(UUID classId) {
        return classRepository.findById(classId).map(ClassView::from).orElseThrow(() -> new NotFoundException("No such class: " + classId));
    }

    @Transactional(readOnly = true)
    public List<ClassView> listByLevel(UUID classLevelId) {
        return classRepository.findByClassLevelIdAndArchivedAtIsNull(classLevelId).stream().map(ClassView::from).toList();
    }

    @Transactional(readOnly = true)
    public ClassSubjectOfferingView getOffering(UUID offeringId) {
        return classSubjectOfferingRepository
                .findById(offeringId)
                .map(ClassSubjectOfferingView::from)
                .orElseThrow(() -> new NotFoundException("No such subject offering: " + offeringId));
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectOfferingView> listOfferings(UUID classId, UUID academicYearId) {
        return classSubjectOfferingRepository.findBySchoolClassIdAndAcademicYearIdAndArchivedAtIsNull(classId, academicYearId)
                .stream()
                .map(ClassSubjectOfferingView::from)
                .toList();
    }
}
