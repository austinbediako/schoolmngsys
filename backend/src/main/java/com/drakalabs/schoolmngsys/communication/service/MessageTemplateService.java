package com.drakalabs.schoolmngsys.communication.service;

import com.drakalabs.schoolmngsys.communication.domain.MessageCategory;
import com.drakalabs.schoolmngsys.communication.domain.MessageChannel;
import com.drakalabs.schoolmngsys.communication.domain.MessageTemplate;
import com.drakalabs.schoolmngsys.communication.repository.MessageTemplateRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;

    public MessageTemplateService(MessageTemplateRepository messageTemplateRepository) {
        this.messageTemplateRepository = messageTemplateRepository;
    }

    @Audited(action = "MESSAGE_TEMPLATE_CREATED", entityType = "MessageTemplate")
    @Transactional
    public MessageTemplateView createTemplate(
            String templateCode,
            String name,
            MessageChannel channel,
            MessageCategory category,
            String subjectTemplate,
            String bodyTemplate,
            boolean active) {
        if (messageTemplateRepository.findByTemplateCodeAndArchivedAtIsNull(templateCode).isPresent()) {
            throw new BusinessRuleViolationException("BR-CO-003", "A template with code " + templateCode + " already exists");
        }
        MessageTemplate template = new MessageTemplate(templateCode, name, channel, category, subjectTemplate, bodyTemplate, active);
        return MessageTemplateView.from(messageTemplateRepository.save(template));
    }

    @Audited(action = "MESSAGE_TEMPLATE_UPDATED", entityType = "MessageTemplate")
    @Transactional
    public MessageTemplateView updateTemplate(
            UUID id,
            String name,
            MessageChannel channel,
            MessageCategory category,
            String subjectTemplate,
            String bodyTemplate,
            boolean active) {
        MessageTemplate template = messageTemplateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("No such message template: " + id));
        template.update(name, channel, category, subjectTemplate, bodyTemplate, active);
        return MessageTemplateView.from(messageTemplateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public MessageTemplateView getByCode(String templateCode) {
        MessageTemplate template = messageTemplateRepository
                .findByTemplateCodeAndArchivedAtIsNull(templateCode)
                .orElseThrow(() -> new NotFoundException("No such message template: " + templateCode));
        return MessageTemplateView.from(template);
    }

    @Transactional(readOnly = true)
    public MessageTemplateView get(UUID id) {
        MessageTemplate template = messageTemplateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("No such message template: " + id));
        return MessageTemplateView.from(template);
    }

    @Transactional(readOnly = true)
    public List<MessageTemplateView> listAll() {
        return messageTemplateRepository.findAll().stream()
                .filter(t -> t.getArchivedAt() == null)
                .map(MessageTemplateView::from)
                .toList();
    }

    public RenderedMessage render(String templateCode, Map<String, String> params) {
        MessageTemplateView template = getByCode(templateCode);
        String subject = interpolate(template.subjectTemplate(), params);
        String body = interpolate(template.bodyTemplate(), params);
        return new RenderedMessage(template.channel(), subject, body);
    }

    private String interpolate(String template, Map<String, String> params) {
        if (template == null) {
            return null;
        }
        String result = template;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return result;
    }

    public record RenderedMessage(MessageChannel channel, String subject, String body) {}
}
