package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StudentDocumentView;
import java.util.UUID;

public record StudentDocumentResponse(UUID id, UUID studentId, String documentType, String originalName, String contentType) {

    public static StudentDocumentResponse from(StudentDocumentView view) {
        return new StudentDocumentResponse(view.id(), view.studentId(), view.documentType(), view.originalName(), view.contentType());
    }
}
