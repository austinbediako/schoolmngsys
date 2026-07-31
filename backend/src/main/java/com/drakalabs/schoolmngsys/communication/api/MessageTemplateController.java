package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.service.MessageTemplateService;
import com.drakalabs.schoolmngsys.communication.service.MessageTemplateView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/message-templates")
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    public MessageTemplateController(MessageTemplateService messageTemplateService) {
        this.messageTemplateService = messageTemplateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MESSAGE_TEMPLATE_MANAGE')")
    public MessageTemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        boolean active = request.active() != null ? request.active() : true;
        MessageTemplateView view = messageTemplateService.createTemplate(
                request.templateCode(),
                request.name(),
                request.channel(),
                request.category(),
                request.subjectTemplate(),
                request.bodyTemplate(),
                active
        );
        return MessageTemplateResponse.from(view);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MESSAGE_TEMPLATE_MANAGE')")
    public MessageTemplateResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTemplateRequest request) {
        boolean active = request.active() != null ? request.active() : true;
        MessageTemplateView view = messageTemplateService.updateTemplate(
                id,
                request.name(),
                request.channel(),
                request.category(),
                request.subjectTemplate(),
                request.bodyTemplate(),
                active
        );
        return MessageTemplateResponse.from(view);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MESSAGE_TEMPLATE_MANAGE')")
    public List<MessageTemplateResponse> listAll() {
        return messageTemplateService.listAll().stream()
                .map(MessageTemplateResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MESSAGE_TEMPLATE_MANAGE')")
    public MessageTemplateResponse get(@PathVariable UUID id) {
        return MessageTemplateResponse.from(messageTemplateService.get(id));
    }
}
