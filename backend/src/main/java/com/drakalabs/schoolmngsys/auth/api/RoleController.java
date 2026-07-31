package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.RoleQueryService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleQueryService roleQueryService;

    public RoleController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public List<RoleResponse> list() {
        return roleQueryService.list().stream().map(RoleResponse::from).toList();
    }
}
