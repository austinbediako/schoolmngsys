package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.Announcement;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementAudienceType;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementStatus;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.repository.AnnouncementRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final OutboxService outboxService;
    private final MessageTemplateService messageTemplateService;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            OutboxService outboxService,
            MessageTemplateService messageTemplateService) {
        this.announcementRepository = announcementRepository;
        this.outboxService = outboxService;
        this.messageTemplateService = messageTemplateService;
    }

    @Audited(action = "ANNOUNCEMENT_CREATED", entityType = "Announcement")
    @Transactional
    public AnnouncementView createAnnouncement(
            String title,
            String content,
            AnnouncementAudienceType audienceType,
            UUID targetAudienceId,
            UUID authorAccountId) {
        Announcement announcement = new Announcement(title, content, audienceType, targetAudienceId, authorAccountId);
        return AnnouncementView.from(announcementRepository.save(announcement));
    }

    @Audited(action = "ANNOUNCEMENT_PUBLISHED", entityType = "Announcement")
    @Transactional
    public AnnouncementView publishAnnouncement(UUID id) {
        Announcement announcement = announcementRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("No such announcement: " + id));
        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) {
            throw new BusinessRuleViolationException("BR-CO-003", "Announcement is already published");
        }
        announcement.publish();
        Announcement saved = announcementRepository.save(announcement);

        // Enqueue outbox notification for announcement
        MessageTemplateService.RenderedMessage rendered = messageTemplateService.render("ANNOUNCEMENT", Map.of(
                "title", saved.getTitle(),
                "content", saved.getContent()
        ));

        outboxService.enqueue(
                "ANNOUNCEMENT",
                rendered.channel(),
                saved.getAudienceType().name(),
                saved.getTargetAudienceId(),
                null,
                null,
                rendered.subject(),
                rendered.body(),
                3
        );

        return AnnouncementView.from(saved);
    }

    @Transactional(readOnly = true)
    public AnnouncementView get(UUID id) {
        Announcement announcement = announcementRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("No such announcement: " + id));
        return AnnouncementView.from(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementView> listPublished() {
        return announcementRepository.findByStatusAndArchivedAtIsNullOrderByPublishedAtDesc(AnnouncementStatus.PUBLISHED).stream()
                .map(AnnouncementView::from)
                .toList();
    }
}
