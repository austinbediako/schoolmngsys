package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountRole;
import com.drakalabs.schoolmngsys.auth.domain.Permission;
import com.drakalabs.schoolmngsys.auth.repository.AccountRoleRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Flattens an account's active role assignments into the permission strings it currently holds. */
@Component
public class PermissionResolver {

    private final AccountRoleRepository accountRoleRepository;

    public PermissionResolver(AccountRoleRepository accountRoleRepository) {
        this.accountRoleRepository = accountRoleRepository;
    }

    @Transactional(readOnly = true)
    public Set<String> resolve(Account account) {
        return accountRoleRepository.findByAccountIdAndArchivedAtIsNull(account.getId()).stream()
                .map(AccountRole::getRole)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
