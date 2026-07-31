package com.drakalabs.schoolmngsys.shared.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.audit.domain.AuditLog;
import com.drakalabs.schoolmngsys.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Proves BR-SE-002/ADR-007: an {@code @Audited} service method writes its audit_log row in the
 * same transaction as the mutation it describes (docs/14 §5 WP-0 test plan).
 */
class AuditAspectIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestWidgetService testWidgetService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void writesAuditLogEntryWhenAnAuditedMethodSucceeds() {
        TestWidget widget = testWidgetService.create("chalkboard");

        assertThat(auditLogRepository.findAll())
                .anySatisfy(
                        entry -> {
                            assertThat(entry.getAction()).isEqualTo("TEST_WIDGET_CREATED");
                            assertThat(entry.getEntityType()).isEqualTo("TestWidget");
                            assertThat(entry.getEntityId()).isEqualTo(widget.getId().toString());
                            assertThat(entry.getOccurredAt()).isNotNull();
                        });
    }
}
