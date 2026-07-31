package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StudentDocumentService;
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

/** FR-STU-01 document attachments — access-checked streaming, never a raw filesystem path (docs/11 §4). */
@RestController
@RequestMapping("/api/v1/students/{studentId}/documents")
public class StudentDocumentController {

    private final StudentDocumentService studentDocumentService;

    public StudentDocumentController(StudentDocumentService studentDocumentService) {
        this.studentDocumentService = studentDocumentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_DOCUMENT_VIEW')")
    public List<StudentDocumentResponse> list(@PathVariable UUID studentId) {
        return studentDocumentService.list(studentId).stream().map(StudentDocumentResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_DOCUMENT_UPLOAD')")
    public StudentDocumentResponse upload(
            @PathVariable UUID studentId, @RequestParam String documentType, @RequestParam MultipartFile file) {
        try {
            return StudentDocumentResponse.from(
                    studentDocumentService.upload(
                            studentId, documentType, file.getOriginalFilename(), file.getContentType(), file.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    @GetMapping("/{documentId}/content")
    @PreAuthorize("hasAuthority('STUDENT_DOCUMENT_VIEW')")
    public ResponseEntity<Resource> download(@PathVariable UUID studentId, @PathVariable UUID documentId) {
        StudentDocumentService.DownloadableDocument document = studentDocumentService.download(studentId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.originalName() + "\"")
                .body(document.resource());
    }
}
