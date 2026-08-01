package com.drakalabs.schoolmngsys.people.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.people.domain.StaffType;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** WP-11: staff-side equivalent of the student document tests — same DocumentStorage seam, same access pattern. */
class StaffDocumentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StaffService staffService;

    @Autowired
    private StaffDocumentService staffDocumentService;

    @Test
    void uploadingAndDownloadingAStaffDocumentRoundTrips() {
        StaffView staff = staffService.createStaff(
                "STAFF-DOC-" + UUID.randomUUID().toString().substring(0, 6),
                "Kwabena",
                "Owusu",
                StaffType.TEACHING,
                null,
                LocalDate.of(2025, 1, 1));

        byte[] content = "Bachelor of Education certificate".getBytes(StandardCharsets.UTF_8);
        StaffDocumentView uploaded = staffDocumentService.upload(
                staff.id(), "QUALIFICATION_CERTIFICATE", "cert.pdf", "application/pdf", new ByteArrayInputStream(content));

        assertThat(uploaded.staffId()).isEqualTo(staff.id());
        assertThat(uploaded.originalName()).isEqualTo("cert.pdf");

        List<StaffDocumentView> documents = staffDocumentService.list(staff.id());
        assertThat(documents).hasSize(1);

        StaffDocumentService.DownloadableDocument downloaded = staffDocumentService.download(staff.id(), uploaded.id());
        assertThat(downloaded.originalName()).isEqualTo("cert.pdf");
        assertThat(downloaded.contentType()).isEqualTo("application/pdf");
    }
}
