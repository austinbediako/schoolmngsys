package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Staff;
import com.drakalabs.schoolmngsys.people.domain.StaffDocument;
import com.drakalabs.schoolmngsys.people.repository.StaffDocumentRepository;
import com.drakalabs.schoolmngsys.people.repository.StaffRepository;
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

/** WP-11: staff-side equivalent of {@link StudentDocumentService} — same DocumentStorage seam, same access-checked streaming (docs/11 §4). */
@Service
public class StaffDocumentService {

    private final StaffRepository staffRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final DocumentStorage documentStorage;

    public StaffDocumentService(
            StaffRepository staffRepository, StaffDocumentRepository staffDocumentRepository, DocumentStorage documentStorage) {
        this.staffRepository = staffRepository;
        this.staffDocumentRepository = staffDocumentRepository;
        this.documentStorage = documentStorage;
    }

    @Audited(action = "STAFF_DOCUMENT_UPLOADED", entityType = "StaffDocument")
    @Transactional
    public StaffDocumentView upload(UUID staffId, String documentType, String originalName, String contentType, InputStream content) {
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("No such staff: " + staffId));

        String storageKey;
        try {
            storageKey = documentStorage.store(originalName, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store document for staff " + staffId, e);
        }

        StaffDocument document = new StaffDocument(staff, documentType, storageKey, originalName, contentType);
        return StaffDocumentView.from(staffDocumentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<StaffDocumentView> list(UUID staffId) {
        return staffDocumentRepository.findByStaffIdAndArchivedAtIsNull(staffId).stream().map(StaffDocumentView::from).toList();
    }

    @Transactional(readOnly = true)
    public DownloadableDocument download(UUID staffId, UUID documentId) {
        StaffDocument document = staffDocumentRepository
                .findById(documentId)
                .filter(candidate -> candidate.getStaff().getId().equals(staffId))
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
