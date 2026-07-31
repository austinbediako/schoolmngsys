package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Student;
import com.drakalabs.schoolmngsys.people.domain.StudentDocument;
import com.drakalabs.schoolmngsys.people.repository.StudentDocumentRepository;
import com.drakalabs.schoolmngsys.people.repository.StudentRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-STU-01 document attachments — metadata here, bytes in {@link DocumentStorage}. */
@Service
public class StudentDocumentService {

    private final StudentRepository studentRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final DocumentStorage documentStorage;

    public StudentDocumentService(
            StudentRepository studentRepository, StudentDocumentRepository studentDocumentRepository, DocumentStorage documentStorage) {
        this.studentRepository = studentRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.documentStorage = documentStorage;
    }

    @Audited(action = "STUDENT_DOCUMENT_UPLOADED", entityType = "StudentDocument")
    @Transactional
    public StudentDocumentView upload(
            UUID studentId, String documentType, String originalName, String contentType, InputStream content) {
        Student student =
                studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("No such student: " + studentId));

        String storageKey;
        try {
            storageKey = documentStorage.store(originalName, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store document for student " + studentId, e);
        }

        StudentDocument document = new StudentDocument(student, documentType, storageKey, originalName, contentType);
        return StudentDocumentView.from(studentDocumentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentView> list(UUID studentId) {
        return studentDocumentRepository.findByStudentIdAndArchivedAtIsNull(studentId).stream()
                .map(StudentDocumentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadableDocument download(UUID studentId, UUID documentId) {
        StudentDocument document = studentDocumentRepository
                .findById(documentId)
                .filter(candidate -> candidate.getStudent().getId().equals(studentId))
                .orElseThrow(() -> new NotFoundException("No such document: " + documentId));

        Resource resource;
        try {
            resource = documentStorage.load(document.getStorageKey());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load document " + documentId, e);
        }
        return new DownloadableDocument(resource, document.getOriginalName(), document.getContentType());
    }

    public record DownloadableDocument(Resource resource, String originalName, String contentType) {
    }
}
