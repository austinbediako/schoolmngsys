package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.domain.MessageCategory;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.service.MessageTemplateView;
import java.util.UUID;

public record MessageTemplateResponse(
        UUID id,
        String templateCode,
        String name,
        MessageChannel channel,
        MessageCategory category,
        String subjectTemplate,
        String bodyTemplate,
        boolean active
) {
    public static MessageTemplateResponse from(MessageTemplateView view) {
        return new MessageTemplateResponse(
                view.id(),
                view.templateCode(),
                view.name(),
                view.channel(),
                view.category(),
                view.subjectTemplate(),
                view.bodyTemplate(),
                view.active()
        );
    }
}
