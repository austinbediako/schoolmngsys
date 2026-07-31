package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountRole;
import com.drakalabs.schoolmngsys.auth.domain.Role;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.AccountRoleRepository;
import com.drakalabs.schoolmngsys.auth.repository.RoleRepository;
import com.drakalabs.schoolmngsys.people.domain.StaffType;
import com.drakalabs.schoolmngsys.people.service.StaffService;
import com.drakalabs.schoolmngsys.people.service.StaffView;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountBootstrapper implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountBootstrapper.class);

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;
    private final StaffService staffService;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenGenerator secureTokenGenerator;

    @Value("${ubs.security.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${ubs.security.bootstrap-admin.email:admin@ubs.edu.gh}")
    private String adminEmail;

    /**
     * Left blank by default on purpose — a fixed default password baked into source (e.g.
     * "Admin123!") is a well-known credential the moment the repository is public, and
     * {@code forcePasswordChange} is not read anywhere on the login path to compensate. When
     * blank, a random one-time password is generated per boot and printed once to the startup
     * log; ops may still pin an explicit value via this property for a controlled first setup.
     */
    @Value("${ubs.security.bootstrap-admin.password:}")
    private String configuredAdminPassword;

    public AdminAccountBootstrapper(
            AccountRepository accountRepository,
            AccountRoleRepository accountRoleRepository,
            RoleRepository roleRepository,
            StaffService staffService,
            PasswordEncoder passwordEncoder,
            SecureTokenGenerator secureTokenGenerator) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.roleRepository = roleRepository;
        this.staffService = staffService;
        this.passwordEncoder = passwordEncoder;
        this.secureTokenGenerator = secureTokenGenerator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (accountRepository.findByLoginIdentifierAndArchivedAtIsNull(adminEmail).isPresent()) {
            return;
        }

        StaffView adminStaff;
        try {
            adminStaff = staffService.createStaff(
                    "STAFF-SYS-ADMIN",
                    "System",
                    "Admin",
                    StaffType.NON_TEACHING,
                    null,
                    LocalDate.of(2025, 1, 1)
            );
        } catch (BusinessRuleViolationException e) {
            log.info("Staff record STAFF-SYS-ADMIN already exists");
            return;
        }

        boolean generated = configuredAdminPassword == null || configuredAdminPassword.isBlank();
        String adminPassword = generated ? secureTokenGenerator.generateOpaqueToken().substring(0, 16) : configuredAdminPassword;

        Account account = new Account(
                PersonType.STAFF,
                adminStaff.id(),
                adminEmail,
                "+233200000000",
                adminEmail,
                passwordEncoder.encode(adminPassword)
        );
        account = accountRepository.save(account);

        assignRoleIfExists(account, "SYSTEM_ADMIN");
        assignRoleIfExists(account, "HEAD_OF_SCHOOL");

        if (generated) {
            log.warn(
                    "Bootstrapped default System Admin account ({}) with a ONE-TIME generated password — "
                            + "retrieve it from THIS log line only, it is never persisted or shown again, and log in to "
                            + "change it immediately: {}",
                    adminEmail,
                    adminPassword);
        } else {
            log.warn(
                    "Bootstrapped default System Admin account ({}) using the configured "
                            + "ubs.security.bootstrap-admin.password — change it immediately after first login.",
                    adminEmail);
        }
        log.info("Successfully bootstrapped default System Admin account with ID: {}", account.getId());
    }

    private void assignRoleIfExists(Account account, String roleName) {
        Optional<Role> roleOpt = roleRepository.findByNameAndArchivedAtIsNull(roleName);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            accountRoleRepository.save(new AccountRole(account, role));
        } else {
            log.warn("Role {} not found during admin bootstrap", roleName);
        }
    }
}
