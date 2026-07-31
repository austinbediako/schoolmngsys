package com.drakalabs.schoolmngsys.communication.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Represents a message template used for outbox notification formatting. */
@Entity
@Table(name = "message_templates")
public class MessageTemplate extends BaseEntity {

    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private MessageCategory category;

    @Column(name = "subject_template", length = 255)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected MessageTemplate() {
        // JPA constructor
    }

    public MessageTemplate(
            String templateCode,
            String name,
            MessageChannel channel,
            MessageCategory category,
            String subjectTemplate,
            String bodyTemplate,
            boolean active) {
        this.templateCode = templateCode;
        this.name = name;
        this.channel = channel;
        this.category = category;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.active = active;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getName() {
        return name;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public MessageCategory getCategory() {
        return category;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, MessageChannel channel, MessageCategory category, String subjectTemplate, String bodyTemplate, boolean active) {
        this.name = name;
        this.channel = channel;
        this.category = category;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.active = active;
    }
}
