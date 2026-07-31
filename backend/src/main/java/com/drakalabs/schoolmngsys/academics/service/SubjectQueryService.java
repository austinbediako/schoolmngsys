package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.repository.SubjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectQueryService {

    private final SubjectRepository subjectRepository;

    public SubjectQueryService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectView> list() {
        return subjectRepository.findAll().stream().map(SubjectView::from).toList();
    }
}
