package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.GuardianQueryService;
import com.drakalabs.schoolmngsys.people.service.GuardianService;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guardians")
public class GuardianController {

    private final GuardianService guardianService;
    private final GuardianQueryService guardianQueryService;

    public GuardianController(GuardianService guardianService, GuardianQueryService guardianQueryService) {
        this.guardianService = guardianService;
        this.guardianQueryService = guardianQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GUARDIAN_VIEW')")
    public PageResponse<GuardianResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(guardianQueryService.list(pageable).map(GuardianResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GUARDIAN_VIEW')")
    public GuardianResponse get(@PathVariable UUID id) {
        return GuardianResponse.from(guardianQueryService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GUARDIAN_CREATE')")
    public GuardianResponse create(@RequestBody @Valid CreateGuardianRequest request) {
        return GuardianResponse.from(
                guardianService.createGuardian(
                        request.firstName(), request.lastName(), request.phone(), request.email(), request.occupation(), request.address()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GUARDIAN_UPDATE')")
    public GuardianResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateGuardianContactRequest request) {
        return GuardianResponse.from(
                guardianService.updateContact(id, request.phone(), request.email(), request.occupation(), request.address()));
    }
}
