package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.StaffDocument;
import java.util.UUID;

public record StaffDocumentView(UUID id, UUID staffId, String documentType, String originalName, String contentType) {

    public static StaffDocumentView from(StaffDocument document) {
        return new StaffDocumentView(
                document.getId(),
                document.getStaff().getId(),
                document.getDocumentType(),
                document.getOriginalName(),
                document.getContentType());
    }
}
