package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.AnnouncementAudienceType;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementStatus;
import com.drakalabs.schoolmngsys.communication.service.AnnouncementView;
import java.time.Instant;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        String title,
        String content,
        AnnouncementAudienceType audienceType,
        UUID targetAudienceId,
        UUID authorAccountId,
        AnnouncementStatus status,
        Instant publishedAt
) {
    public static AnnouncementResponse from(AnnouncementView view) {
        return new AnnouncementResponse(
                view.id(),
                view.title(),
                view.content(),
                view.audienceType(),
                view.targetAudienceId(),
                view.authorAccountId(),
                view.status(),
                view.publishedAt()
        );
    }
}
