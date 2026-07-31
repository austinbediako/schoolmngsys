package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.AnnouncementAudienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAnnouncementRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Content is required") String content,
        @NotNull(message = "Audience type is required") AnnouncementAudienceType audienceType,
        UUID targetAudienceId
) {}
