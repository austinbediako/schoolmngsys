package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.MessageCategory;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.MessageTemplate;
import java.util.UUID;

public record MessageTemplateView(
        UUID id,
        String templateCode,
        String name,
        MessageChannel channel,
        MessageCategory category,
        String subjectTemplate,
        String bodyTemplate,
        boolean active
) {
    public static MessageTemplateView from(MessageTemplate template) {
        return new MessageTemplateView(
                template.getId(),
                template.getTemplateCode(),
                template.getName(),
                template.getChannel(),
                template.getCategory(),
                template.getSubjectTemplate(),
                template.getBodyTemplate(),
                template.isActive()
        );
    }
}
