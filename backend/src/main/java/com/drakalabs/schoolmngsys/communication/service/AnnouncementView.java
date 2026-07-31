package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.Announcement;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementAudienceType;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementStatus;
import java.time.Instant;
import java.util.UUID;

public record AnnouncementView(
        UUID id,
        String title,
        String content,
        AnnouncementAudienceType audienceType,
        UUID targetAudienceId,
        UUID authorAccountId,
        AnnouncementStatus status,
        Instant publishedAt
) {
    public static AnnouncementView from(Announcement announcement) {
        return new AnnouncementView(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getAudienceType(),
                announcement.getTargetAudienceId(),
                announcement.getAuthorAccountId(),
                announcement.getStatus(),
                announcement.getPublishedAt()
        );
    }
}
