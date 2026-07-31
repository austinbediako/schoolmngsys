package com.drakalabs.schoolmngsys.communication.api;

import com.drakalabs.schoolmngsys.communication.service.AnnouncementService;
import com.drakalabs.schoolmngsys.communication.service.AnnouncementView;
import com.drakalabs.schoolmngsys.shared.security.CurrentAccountProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final CurrentAccountProvider currentAccountProvider;

    public AnnouncementController(
            AnnouncementService announcementService,
            CurrentAccountProvider currentAccountProvider) {
        this.announcementService = announcementService;
        this.currentAccountProvider = currentAccountProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_CREATE')")
    public AnnouncementResponse create(@Valid @RequestBody CreateAnnouncementRequest request) {
        UUID authorId = currentAccountProvider.current()
                .map(acc -> acc.accountId())
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        AnnouncementView view = announcementService.createAnnouncement(
                request.title(),
                request.content(),
                request.audienceType(),
                request.targetAudienceId(),
                authorId
        );
        return AnnouncementResponse.from(view);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_CREATE')")
    public AnnouncementResponse publish(@PathVariable UUID id) {
        return AnnouncementResponse.from(announcementService.publishAnnouncement(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_VIEW')")
    public List<AnnouncementResponse> listPublished() {
        return announcementService.listPublished().stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_VIEW')")
    public AnnouncementResponse get(@PathVariable UUID id) {
        return AnnouncementResponse.from(announcementService.get(id));
    }
}
