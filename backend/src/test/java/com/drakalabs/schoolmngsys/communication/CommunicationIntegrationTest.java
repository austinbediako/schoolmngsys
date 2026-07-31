package com.drakalabs.schoolmngsys.communication;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.repository.ClassLevelRepository;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import com.drakalabs.schoolmngsys.academics.service.ClassService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermSpec;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementAudienceType;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementStatus;
import com.drakalabs.schoolmngsys.communication.domain.DeliveryStatus;
import com.drakalabs.schoolmngsys.communication.domain.MessageCategory;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.OutboxStatus;
import com.drakalabs.schoolmngsys.communication.service.AnnouncementService;
import com.drakalabs.schoolmngsys.communication.service.AnnouncementView;
import com.drakalabs.schoolmngsys.communication.service.CommunicationQueryService;
import com.drakalabs.schoolmngsys.communication.service.MessageTemplateService;
import com.drakalabs.schoolmngsys.communication.service.MessageTemplateView;
import com.drakalabs.schoolmngsys.communication.service.NotificationDeliveryView;
import com.drakalabs.schoolmngsys.communication.service.OutboxDispatcher;
import com.drakalabs.schoolmngsys.communication.service.OutboxMessageView;
import com.drakalabs.schoolmngsys.communication.service.OutboxService;
import com.drakalabs.schoolmngsys.communication.service.provider.SmsAdapter;
import com.drakalabs.schoolmngsys.communication.service.provider.SmsSendResult;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentService;
import com.drakalabs.schoolmngsys.finance.domain.PaymentChannel;
import com.drakalabs.schoolmngsys.finance.service.BillingRunService;
import com.drakalabs.schoolmngsys.finance.service.FeeItemSpec;
import com.drakalabs.schoolmngsys.finance.service.FeeScheduleService;
import com.drakalabs.schoolmngsys.finance.service.FeeScheduleView;
import com.drakalabs.schoolmngsys.finance.service.PaymentService;
import com.drakalabs.schoolmngsys.people.domain.Gender;
import com.drakalabs.schoolmngsys.people.domain.RelationshipType;
import com.drakalabs.schoolmngsys.people.service.GuardianLinkSpec;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;

/**
 * WP-8 (docs/14 §5): outbox written in same transaction as domain change; retry/backoff;
 * provider outage doesn't fail the workflow (NFR-08); guardian message consolidation, announcements.
 */
class CommunicationIntegrationTest extends AbstractIntegrationTest {

    /** A recipient phone that the test {@link SmsAdapter} below treats as a simulated provider outage. */
    private static final String FAILURE_TRIGGER_PHONE = "+000000000000";

    @TestConfiguration
    static class FailureInjectingSmsAdapterConfig {

        @Bean
        @Primary
        SmsAdapter failureInjectingSmsAdapter() {
            return new SmsAdapter() {
                @Override
                public SmsSendResult sendSms(String recipientPhone, String message) {
                    if (FAILURE_TRIGGER_PHONE.equals(recipientPhone)) {
                        return SmsSendResult.failure(providerName(), "Simulated provider outage");
                    }
                    return SmsSendResult.success(providerName(), "SMS-" + UUID.randomUUID());
                }

                @Override
                public String providerName() {
                    return "TestSmsProvider";
                }
            };
        }
    }

    @Autowired
    private MessageTemplateService messageTemplateService;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxDispatcher outboxDispatcher;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CommunicationQueryService communicationQueryService;

    @Autowired
    private BillingRunService billingRunService;

    @Autowired
    private FeeScheduleService feeScheduleService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private AcademicYearQueryService academicYearQueryService;

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private GuardianService guardianService;

    private int phoneCounter = 7000000;

    private AcademicYearView newYear() {
        return academicYearService.createYear(
                "Y-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 8, 1),
                List.of(
                        new TermSpec(1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15), 70),
                        new TermSpec(2, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 4, 4), 60),
                        new TermSpec(3, LocalDate.of(2027, 4, 25), LocalDate.of(2027, 8, 1), 65)));
    }

    private UUID termIdOf(AcademicYearView year, int termNumber) {
        return academicYearQueryService.listTerms(year.id()).stream()
                .filter(term -> term.termNumber() == termNumber)
                .findFirst()
                .orElseThrow()
                .id();
    }

    private UUID classLevelId(String code) {
        return classLevelRepository.findByCodeAndArchivedAtIsNull(code).orElseThrow().getId();
    }

    private UUID newEnrollment(AcademicYearView year, UUID classId) {
        GuardianView guardian = guardianService.createGuardian("Kwame", "Mensah", "+23324" + (phoneCounter++), null, null, null);
        StudentView student = studentService.createStudent(
                "Ama",
                "Mensah",
                null,
                LocalDate.of(2016, 5, 12),
                Gender.FEMALE,
                LocalDate.of(2026, 9, 1),
                List.of(new GuardianLinkSpec(guardian.id(), RelationshipType.MOTHER, true, true, true, true)));
        return enrollmentService.enroll(student.id(), classId, year.id(), null).id();
    }

    @Test
    void messageTemplateCanBeCreatedAndRendered() {
        String code = "TEST_TEMPLATE_" + UUID.randomUUID().toString().substring(0, 8);
        MessageTemplateView template = messageTemplateService.createTemplate(
                code,
                "Test Template",
                MessageChannel.SMS,
                MessageCategory.TRANSACTIONAL,
                "Notice for {name}",
                "Hello {name}, your balance is GHS {amount}.",
                true
        );

        assertThat(template.templateCode()).isEqualTo(code);

        MessageTemplateService.RenderedMessage rendered = messageTemplateService.render(code, java.util.Map.of("name", "Kofi", "amount", "150.00"));
        assertThat(rendered.subject()).isEqualTo("Notice for Kofi");
        assertThat(rendered.body()).isEqualTo("Hello Kofi, your balance is GHS 150.00.");
    }

    @Test
    void outboxMessageIsProcessedAndLoggedOnDispatch() {
        OutboxMessageView enqueued = outboxService.enqueue(
                "TEST_CODE",
                MessageChannel.SMS,
                "GUARDIAN",
                UUID.randomUUID(),
                "+233241234567",
                null,
                "Subject",
                "Body content",
                3
        );

        assertThat(enqueued.status()).isEqualTo(OutboxStatus.PENDING);

        outboxDispatcher.processSingleMessage(enqueued.id());

        OutboxMessageView updated = communicationQueryService.getOutbox(enqueued.id());
        assertThat(updated.status()).isEqualTo(OutboxStatus.SENT);
        assertThat(updated.providerName()).isNotNull();

        List<NotificationDeliveryView> deliveries = communicationQueryService.listDeliveriesForOutbox(enqueued.id());
        assertThat(deliveries).hasSize(1);
        assertThat(deliveries.get(0).status()).isEqualTo(DeliveryStatus.SUCCESS);
        assertThat(deliveries.get(0).recipient()).isEqualTo("+233241234567");
    }

    @Test
    void announcementCanBeCreatedAndPublished() {
        AnnouncementView draft = announcementService.createAnnouncement(
                "End of Term Notice",
                "School closes at 2 PM this Friday.",
                AnnouncementAudienceType.SCHOOL,
                null,
                UUID.randomUUID()
        );

        assertThat(draft.status()).isEqualTo(AnnouncementStatus.DRAFT);

        AnnouncementView published = announcementService.publishAnnouncement(draft.id());
        assertThat(published.status()).isEqualTo(AnnouncementStatus.PUBLISHED);
        assertThat(published.publishedAt()).isNotNull();

        List<AnnouncementView> list = announcementService.listPublished();
        assertThat(list).extracting(AnnouncementView::id).contains(draft.id());
    }

    @Test
    void billingAndPaymentEmitEventsThatQueueOutboxMessages() {
        AcademicYearView year = newYear();
        UUID termId = termIdOf(year, 1);
        UUID classLevelId = classLevelId("B1");
        ClassView schoolClass = classService.createClass("B1", "A-" + UUID.randomUUID(), 30);

        FeeScheduleView schedule = feeScheduleService.create(classLevelId, termId, List.of(new FeeItemSpec("Tuition", new BigDecimal("400.00"), true)));
        feeScheduleService.approve(schedule.id());

        UUID enrollmentId = newEnrollment(year, schoolClass.id());

        // 1. Run Billing -> triggers InvoiceIssued -> queues INVOICE_ISSUED outbox message
        billingRunService.runBilling(classLevelId, termId);

        List<OutboxMessageView> outboxList1 = communicationQueryService.listOutbox(PageRequest.of(0, 100)).getContent();
        assertThat(outboxList1).anySatisfy(msg -> {
            assertThat(msg.templateCode()).isEqualTo("INVOICE_ISSUED");
            assertThat(msg.body()).contains("GHS 400.00");
        });

        // 2. Record Payment -> triggers PaymentReceived -> queues PAYMENT_RECEIPT outbox message
        paymentService.recordPayment(enrollmentId, new BigDecimal("400.00"), PaymentChannel.CASH, null, null, null);

        List<OutboxMessageView> outboxList2 = communicationQueryService.listOutbox(PageRequest.of(0, 100)).getContent();
        assertThat(outboxList2).anySatisfy(msg -> {
            assertThat(msg.templateCode()).isEqualTo("PAYMENT_RECEIPT");
            assertThat(msg.body()).contains("GHS 400.00");
        });
    }

    @Test
    void aProviderOutageIsAbsorbedAsARetryNotAWorkflowFailure() {
        OutboxMessageView enqueued = outboxService.enqueue(
                "TEST_OUTAGE",
                MessageChannel.SMS,
                "GUARDIAN",
                UUID.randomUUID(),
                FAILURE_TRIGGER_PHONE,
                null,
                "Subject",
                "Body content",
                3
        );

        // NFR-08: the dispatch call itself must not throw even though the "provider" fails.
        outboxDispatcher.processSingleMessage(enqueued.id());

        OutboxMessageView afterFirstAttempt = communicationQueryService.getOutbox(enqueued.id());
        assertThat(afterFirstAttempt.status()).isEqualTo(OutboxStatus.PENDING); // retried, not yet exhausted
        assertThat(afterFirstAttempt.retryCount()).isEqualTo(1);
        assertThat(afterFirstAttempt.nextAttemptAt()).isNotNull().isAfter(java.time.Instant.now());

        List<NotificationDeliveryView> deliveriesAfterFirst = communicationQueryService.listDeliveriesForOutbox(enqueued.id());
        assertThat(deliveriesAfterFirst).hasSize(1);
        assertThat(deliveriesAfterFirst.get(0).status()).isEqualTo(DeliveryStatus.FAILED);

        // Exhaust the remaining retries (maxRetries = 3) — still must never throw.
        outboxDispatcher.processSingleMessage(enqueued.id());
        outboxDispatcher.processSingleMessage(enqueued.id());

        OutboxMessageView exhausted = communicationQueryService.getOutbox(enqueued.id());
        assertThat(exhausted.status()).isEqualTo(OutboxStatus.FAILED);
        assertThat(exhausted.retryCount()).isEqualTo(3);

        List<NotificationDeliveryView> deliveriesFinal = communicationQueryService.listDeliveriesForOutbox(enqueued.id());
        assertThat(deliveriesFinal).hasSize(3);
        assertThat(deliveriesFinal).allSatisfy(delivery -> assertThat(delivery.status()).isEqualTo(DeliveryStatus.FAILED));
    }
}
