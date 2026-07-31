package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.AccountProvisioningService;
import com.drakalabs.schoolmngsys.auth.service.AccountQueryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageRequestFactory;
import com.drakalabs.schoolmngsys.shared.web.pagination.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountQueryService accountQueryService;
    private final AccountProvisioningService accountProvisioningService;

    public AccountController(AccountQueryService accountQueryService, AccountProvisioningService accountProvisioningService) {
        this.accountQueryService = accountQueryService;
        this.accountProvisioningService = accountProvisioningService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public PageResponse<AccountResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestFactory.of(page, size);
        return PageResponse.from(accountQueryService.list(pageable).map(AccountResponse::from));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNT_CREATE')")
    public CreateAccountResponse create(@RequestBody @Valid CreateAccountRequest request) {
        return CreateAccountResponse.from(
                accountProvisioningService.createAccount(
                        request.personType(),
                        request.personId(),
                        request.loginIdentifier(),
                        request.phone(),
                        request.email()));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ACCOUNT_DEACTIVATE')")
    public AccountResponse deactivate(@PathVariable UUID id) {
        return AccountResponse.from(accountProvisioningService.deactivateAccount(id));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public AccountResponse assignRole(@PathVariable UUID id, @RequestBody @Valid AssignRoleRequest request) {
        return AccountResponse.from(accountProvisioningService.assignRole(id, request.roleName()));
    }
}
