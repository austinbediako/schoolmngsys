package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassQueryService;
import com.drakalabs.schoolmngsys.academics.service.ClassView;
import com.drakalabs.schoolmngsys.academics.service.TermView;
import com.drakalabs.schoolmngsys.assessment.domain.TermResultsPublished;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentQueryService;
import com.drakalabs.schoolmngsys.enrollment.service.EnrollmentView;
import com.drakalabs.schoolmngsys.finance.domain.InvoiceIssued;
import com.drakalabs.schoolmngsys.finance.domain.PaymentReceived;
import com.drakalabs.schoolmngsys.people.service.GuardianQueryService;
import com.drakalabs.schoolmngsys.people.service.GuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentGuardianView;
import com.drakalabs.schoolmngsys.people.service.StudentQueryService;
import com.drakalabs.schoolmngsys.people.service.StudentView;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to domain events across modules and enqueues outbox notifications (ADR-008, BR-CO-002, BR-CO-003).
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final OutboxService outboxService;
    private final MessageTemplateService messageTemplateService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final StudentQueryService studentQueryService;
    private final GuardianQueryService guardianQueryService;
    private final ClassQueryService classQueryService;
    private final AcademicYearQueryService academicYearQueryService;

    public NotificationEventListener(
            OutboxService outboxService,
            MessageTemplateService messageTemplateService,
            EnrollmentQueryService enrollmentQueryService,
            StudentQueryService studentQueryService,
            GuardianQueryService guardianQueryService,
            ClassQueryService classQueryService,
            AcademicYearQueryService academicYearQueryService) {
        this.outboxService = outboxService;
        this.messageTemplateService = messageTemplateService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.studentQueryService = studentQueryService;
        this.guardianQueryService = guardianQueryService;
        this.classQueryService = classQueryService;
        this.academicYearQueryService = academicYearQueryService;
    }

    @EventListener
    public void handleTermResultsPublished(TermResultsPublished event) {
        log.info("Handling TermResultsPublished for classId: {}", event.getClassId());
        ClassView schoolClass = classQueryService.get(event.getClassId());
        TermView term = academicYearQueryService.getTerm(event.getTermId());

        for (java.util.UUID enrollmentId : event.getEnrollmentIds()) {
            try {
                EnrollmentView enrollment = enrollmentQueryService.get(enrollmentId);
                StudentView student = studentQueryService.get(enrollment.studentId());
                List<StudentGuardianView> guardianLinks = studentQueryService.listGuardianLinks(student.id());

                for (StudentGuardianView link : guardianLinks) {
                    if (link.primaryContact() || link.receivesAcademicReports()) {
                        GuardianView guardian = guardianQueryService.get(link.guardianId());
                        MessageTemplateService.RenderedMessage rendered = messageTemplateService.render(
                                "RESULT_PUBLISHED",
                                Map.of(
                                        "guardianName", guardian.firstName() + " " + guardian.lastName(),
                                        "studentName", student.firstName() + " " + student.lastName(),
                                        "className", schoolClass.classLevelCode() + " " + schoolClass.stream(),
                                        "termName", "Term " + term.termNumber()
                                )
                        );

                        outboxService.enqueue(
                                "RESULT_PUBLISHED",
                                MessageChannel.SMS,
                                "GUARDIAN",
                                guardian.id(),
                                guardian.phone(),
                                guardian.email(),
                                rendered.subject(),
                                rendered.body(),
                                3
                        );
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process result notification for enrollment {}: {}", enrollmentId, e.getMessage(), e);
            }
        }
    }

    @EventListener
    public void handleInvoiceIssued(InvoiceIssued event) {
        log.info("Handling InvoiceIssued for invoiceId: {}", event.getInvoiceId());
        try {
            EnrollmentView enrollment = enrollmentQueryService.get(event.getEnrollmentId());
            StudentView student = studentQueryService.get(enrollment.studentId());
            TermView term = academicYearQueryService.getTerm(event.getTermId());
            List<StudentGuardianView> guardianLinks = studentQueryService.listGuardianLinks(student.id());

            for (StudentGuardianView link : guardianLinks) {
                if (link.receivesBilling() || link.primaryContact()) {
                    GuardianView guardian = guardianQueryService.get(link.guardianId());
                    MessageTemplateService.RenderedMessage rendered = messageTemplateService.render(
                            "INVOICE_ISSUED",
                            Map.of(
                                    "guardianName", guardian.firstName() + " " + guardian.lastName(),
                                    "studentName", student.firstName() + " " + student.lastName(),
                                    "termName", "Term " + term.termNumber(),
                                    "amount", event.getTotalAmount().toString()
                            )
                    );

                    outboxService.enqueue(
                            "INVOICE_ISSUED",
                            MessageChannel.SMS,
                            "GUARDIAN",
                            guardian.id(),
                            guardian.phone(),
                            guardian.email(),
                            rendered.subject(),
                            rendered.body(),
                            3
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to process invoice notification for invoice {}: {}", event.getInvoiceId(), e.getMessage(), e);
        }
    }

    @EventListener
    public void handlePaymentReceived(PaymentReceived event) {
        log.info("Handling PaymentReceived for paymentId: {}", event.getPaymentId());
        try {
            EnrollmentView enrollment = enrollmentQueryService.get(event.getEnrollmentId());
            StudentView student = studentQueryService.get(enrollment.studentId());
            List<StudentGuardianView> guardianLinks = studentQueryService.listGuardianLinks(student.id());

            for (StudentGuardianView link : guardianLinks) {
                if (link.receivesBilling() || link.primaryContact()) {
                    GuardianView guardian = guardianQueryService.get(link.guardianId());
                    MessageTemplateService.RenderedMessage rendered = messageTemplateService.render(
                            "PAYMENT_RECEIPT",
                            Map.of(
                                    "guardianName", guardian.firstName() + " " + guardian.lastName(),
                                    "studentName", student.firstName() + " " + student.lastName(),
                                    "receiptNumber", event.getReceiptNumber(),
                                    "amount", event.getAmount().toString()
                            )
                    );

                    outboxService.enqueue(
                            "PAYMENT_RECEIPT",
                            MessageChannel.SMS,
                            "GUARDIAN",
                            guardian.id(),
                            guardian.phone(),
                            guardian.email(),
                            rendered.subject(),
                            rendered.body(),
                            3
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to process payment notification for payment {}: {}", event.getPaymentId(), e.getMessage(), e);
        }
    }
}
