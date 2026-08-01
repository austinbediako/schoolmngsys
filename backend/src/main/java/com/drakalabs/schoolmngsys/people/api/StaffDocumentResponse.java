package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StaffDocumentView;
import java.util.UUID;

public record StaffDocumentResponse(UUID id, UUID staffId, String documentType, String originalName, String contentType) {

    public static StaffDocumentResponse from(StaffDocumentView view) {
        return new StaffDocumentResponse(view.id(), view.staffId(), view.documentType(), view.originalName(), view.contentType());
    }
}
