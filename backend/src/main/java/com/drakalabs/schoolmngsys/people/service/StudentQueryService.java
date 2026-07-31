package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.repository.StudentGuardianRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentQueryService {

    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;

    public StudentQueryService(StudentRepository studentRepository, StudentGuardianRepository studentGuardianRepository) {
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
    }

    @Transactional(readOnly = true)
    public Page<StudentView> list(Pageable pageable) {
        return studentRepository.findAll(pageable).map(StudentView::from);
    }

    @Transactional(readOnly = true)
    public StudentView get(UUID studentId) {
        return studentRepository
                .findById(studentId)
                .map(StudentView::from)
                .orElseThrow(() -> new NotFoundException("No such student: " + studentId));
    }

    @Transactional(readOnly = true)
    public List<StudentGuardianView> listGuardianLinks(UUID studentId) {
        return studentGuardianRepository.findByStudentIdAndArchivedAtIsNull(studentId).stream()
                .map(StudentGuardianView::from)
                .toList();
    }
}
