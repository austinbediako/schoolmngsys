package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StaffDocumentService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** WP-11 — staff-side equivalent of {@code StudentDocumentController}: access-checked streaming, never a raw filesystem path (docs/11 §4). */
@RestController
@RequestMapping("/api/v1/staff/{staffId}/documents")
public class StaffDocumentController {

    private final StaffDocumentService staffDocumentService;

    public StaffDocumentController(StaffDocumentService staffDocumentService) {
        this.staffDocumentService = staffDocumentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF_DOCUMENT_VIEW')")
    public List<StaffDocumentResponse> list(@PathVariable UUID staffId) {
        return staffDocumentService.list(staffId).stream().map(StaffDocumentResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF_DOCUMENT_UPLOAD')")
    public StaffDocumentResponse upload(@PathVariable UUID staffId, @RequestParam String documentType, @RequestParam MultipartFile file) {
        try {
            return StaffDocumentResponse.from(
                    staffDocumentService.upload(staffId, documentType, file.getOriginalFilename(), file.getContentType(), file.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    @GetMapping("/{documentId}/content")
    @PreAuthorize("hasAuthority('STAFF_DOCUMENT_VIEW')")
    public ResponseEntity<Resource> download(@PathVariable UUID staffId, @PathVariable UUID documentId) {
        StaffDocumentService.DownloadableDocument document = staffDocumentService.download(staffId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.originalName() + "\"")
                .body(document.resource());
    }
}
