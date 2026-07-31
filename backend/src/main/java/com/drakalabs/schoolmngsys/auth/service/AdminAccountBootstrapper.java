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

    @Value("${ubs.security.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${ubs.security.bootstrap-admin.email:admin@ubs.edu.gh}")
    private String adminEmail;

    @Value("${ubs.security.bootstrap-admin.password:Admin123!}")
    private String adminPassword;

    @Value("${ubs.security.bootstrap-teacher.email:teacher@ubs.edu.gh}")
    private String teacherEmail;

    @Value("${ubs.security.bootstrap-teacher.password:Teacher123!}")
    private String teacherPassword;

    public AdminAccountBootstrapper(
            AccountRepository accountRepository,
            AccountRoleRepository accountRoleRepository,
            RoleRepository roleRepository,
            StaffService staffService,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.roleRepository = roleRepository;
        this.staffService = staffService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        bootstrapAdmin();
        bootstrapTeacher();
    }

    private void bootstrapAdmin() {
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

        log.info("Successfully bootstrapped System Admin account: {} / {}", adminEmail, adminPassword);
    }

    private void bootstrapTeacher() {
        if (accountRepository.findByLoginIdentifierAndArchivedAtIsNull(teacherEmail).isPresent()) {
            return;
        }

        StaffView teacherStaff;
        try {
            teacherStaff = staffService.createStaff(
                    "STAFF-TEACHER-01",
                    "Kofi",
                    "Annan",
                    StaffType.TEACHING,
                    null,
                    LocalDate.of(2025, 1, 1)
            );
        } catch (BusinessRuleViolationException e) {
            log.info("Staff record STAFF-TEACHER-01 already exists");
            return;
        }

        Account account = new Account(
                PersonType.STAFF,
                teacherStaff.id(),
                teacherEmail,
                "+233240000001",
                teacherEmail,
                passwordEncoder.encode(teacherPassword)
        );
        account = accountRepository.save(account);

        assignRoleIfExists(account, "TEACHER");

        log.info("Successfully bootstrapped Staff Teacher account: {} / {}", teacherEmail, teacherPassword);
    }

    private void assignRoleIfExists(Account account, String roleName) {
        Optional<Role> roleOpt = roleRepository.findByNameAndArchivedAtIsNull(roleName);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            accountRoleRepository.save(new AccountRole(account, role));
        } else {
            log.warn("Role {} not found during bootstrap", roleName);
        }
    }
}
