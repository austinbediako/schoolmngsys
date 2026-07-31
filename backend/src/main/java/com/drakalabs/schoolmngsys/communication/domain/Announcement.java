package com.drakalabs.schoolmngsys.communication.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Audience-targeted school announcement (FR-COM-03, BR-CO-003). */
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false, length = 30)
    private AnnouncementAudienceType audienceType;

    @Column(name = "target_audience_id")
    private UUID targetAudienceId;

    @Column(name = "author_account_id", nullable = false)
    private UUID authorAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected Announcement() {
        // JPA constructor
    }

    public Announcement(
            String title,
            String content,
            AnnouncementAudienceType audienceType,
            UUID targetAudienceId,
            UUID authorAccountId) {
        this.title = title;
        this.content = content;
        this.audienceType = audienceType;
        this.targetAudienceId = targetAudienceId;
        this.authorAccountId = authorAccountId;
        this.status = AnnouncementStatus.DRAFT;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public AnnouncementAudienceType getAudienceType() {
        return audienceType;
    }

    public UUID getTargetAudienceId() {
        return targetAudienceId;
    }

    public UUID getAuthorAccountId() {
        return authorAccountId;
    }

    public AnnouncementStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void publish() {
        this.status = AnnouncementStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void archive() {
        this.status = AnnouncementStatus.ARCHIVED;
    }
}
