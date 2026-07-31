package com.drakalabs.schoolmngsys.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.audit.domain.AuditLog;
import com.drakalabs.schoolmngsys.audit.repository.AuditLogRepository;
import com.drakalabs.schoolmngsys.audit.service.AuditLogQueryService;
import com.drakalabs.schoolmngsys.audit.service.AuditLogView;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class AuditLogQueryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogQueryService auditLogQueryService;

    @Test
    void listAuditLogsFiltersByEntityTypeAndActor() {
        UUID actorId = UUID.randomUUID();
        AuditLog entry1 = new AuditLog(actorId, "STUDENT_CREATED", "Student", "STU-001", Map.of("name", "Kofi"), "127.0.0.1");
        AuditLog entry2 = new AuditLog(actorId, "FEE_SCHEDULE_CREATED", "FeeSchedule", "SCH-001", Map.of("term", "Term 1"), "127.0.0.1");

        auditLogRepository.save(entry1);
        auditLogRepository.save(entry2);

        Page<AuditLogView> studentAudits = auditLogQueryService.listAuditLogs("Student", null, actorId, null, null, PageRequest.of(0, 10));
        assertThat(studentAudits.getContent()).hasSize(1);
        assertThat(studentAudits.getContent().get(0).entityId()).isEqualTo("STU-001");

        Page<AuditLogView> actorAudits = auditLogQueryService.listAuditLogs(null, null, actorId, null, null, PageRequest.of(0, 10));
        assertThat(actorAudits.getContent()).hasSize(2);
    }
}
