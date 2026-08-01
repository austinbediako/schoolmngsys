package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Attachment metadata only — the file itself lives in {@code DocumentStorage} (docs/11 §4). WP-11: staff-side equivalent of {@link StudentDocument}. */
@Entity
@Table(name = "staff_documents")
public class StaffDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    protected StaffDocument() {
    }

    public StaffDocument(Staff staff, String documentType, String storageKey, String originalName, String contentType) {
        this.staff = staff;
        this.documentType = documentType;
        this.storageKey = storageKey;
        this.originalName = originalName;
        this.contentType = contentType;
    }

    public Staff getStaff() {
        return staff;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }
}
