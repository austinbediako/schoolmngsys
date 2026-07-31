package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountRole;
import com.drakalabs.schoolmngsys.auth.domain.RefreshToken;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import com.drakalabs.schoolmngsys.auth.domain.Role;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.AccountRoleRepository;
import com.drakalabs.schoolmngsys.auth.repository.RefreshTokenRepository;
import com.drakalabs.schoolmngsys.auth.repository.RoleRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account lifecycle: provisioning (never self-registration, BR-SE-003), role assignment, and
 * deactivation (BR-ST-002/FR-AUTH-04 — deactivation revokes refresh capability immediately).
 */
@Service
public class AccountProvisioningService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenGenerator tokenGenerator;

    public AccountProvisioningService(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            AccountRoleRepository accountRoleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            SecureTokenGenerator tokenGenerator) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Audited(action = "ACCOUNT_CREATED", entityType = "Account")
    @Transactional
    public AccountCreationResult createAccount(
            PersonType personType, UUID personId, String loginIdentifier, String phone, String email) {
        String temporaryPassword = tokenGenerator.generateOpaqueToken().substring(0, 12);
        Account account = new Account(
                personType, personId, loginIdentifier, phone, email, passwordEncoder.encode(temporaryPassword));
        accountRepository.save(account);
        return new AccountCreationResult(AccountView.from(account), temporaryPassword);
    }

    @Audited(action = "ROLE_ASSIGNED", entityType = "AccountRole")
    @Transactional
    public AccountView assignRole(UUID accountId, String roleName) {
        Account account = getAccount(accountId);
        Role role = roleRepository
                .findByNameAndArchivedAtIsNull(roleName)
                .orElseThrow(() -> new NotFoundException("No such role: " + roleName));

        boolean alreadyAssigned =
                accountRoleRepository.findByAccountIdAndArchivedAtIsNull(accountId).stream()
                        .anyMatch(existing -> existing.getRole().getId().equals(role.getId()));
        if (alreadyAssigned) {
            throw new BusinessRuleViolationException("BR-SE-003", "Account already has role " + roleName);
        }

        accountRoleRepository.save(new AccountRole(account, role));
        return AccountView.from(account);
    }

    @Audited(action = "ACCOUNT_DEACTIVATED", entityType = "Account")
    @Transactional
    public AccountView deactivateAccount(UUID accountId) {
        Account account = getAccount(accountId);
        account.deactivate();
        accountRepository.save(account);

        for (RefreshToken token : refreshTokenRepository.findByAccountIdAndRevokedAtIsNull(accountId)) {
            token.revoke(null);
            refreshTokenRepository.save(token);
        }
        return AccountView.from(account);
    }

    private Account getAccount(UUID accountId) {
        return accountRepository
                .findById(accountId)
                .orElseThrow(() -> new NotFoundException("No such account: " + accountId));
    }
}
