package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.StudentDocument;
import java.util.UUID;

public record StudentDocumentView(UUID id, UUID studentId, String documentType, String originalName, String contentType) {

    public static StudentDocumentView from(StudentDocument document) {
        return new StudentDocumentView(
                document.getId(),
                document.getStudent().getId(),
                document.getDocumentType(),
                document.getOriginalName(),
                document.getContentType());
    }
}
