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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
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

    @Value("${ubs.security.bootstrap-admin.password:Admin123!}")
    private String configuredAdminPassword;

    @Value("${ubs.security.bootstrap-teacher.email:teacher@ubs.edu.gh}")
    private String teacherEmail;

    @Value("${ubs.security.bootstrap-teacher.password:Teacher123!}")
    private String configuredTeacherPassword;

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

        bootstrapAdmin();
        bootstrapTeacher();
    }

    private void bootstrapAdmin() {
        if (accountRepository.findByLoginIdentifierAndArchivedAtIsNull(adminEmail).isPresent()) {
            return;
        }

        UUID adminStaffId;
        Optional<StaffView> existingStaff = staffService.findStaffByStaffNumber("STAFF-SYS-ADMIN");
        if (existingStaff.isPresent()) {
            adminStaffId = existingStaff.get().id();
        } else {
            StaffView adminStaff = staffService.createStaff(
                    "STAFF-SYS-ADMIN",
                    "System",
                    "Admin",
                    StaffType.NON_TEACHING,
                    null,
                    LocalDate.of(2025, 1, 1)
            );
            adminStaffId = adminStaff.id();
        }

        boolean generated = configuredAdminPassword == null || configuredAdminPassword.isBlank();
        String adminPassword = generated ? secureTokenGenerator.generateOpaqueToken().substring(0, 16) : configuredAdminPassword;

        Account account = new Account(
                PersonType.STAFF,
                adminStaffId,
                adminEmail,
                "+233200000000",
                adminEmail,
                passwordEncoder.encode(adminPassword)
        );
        account = accountRepository.save(account);

        assignRoleIfExists(account, "SYSTEM_ADMIN");
        assignRoleIfExists(account, "HEAD_OF_SCHOOL");

        log.info("Bootstrapped System Admin account ({}) successfully", adminEmail);
    }

    private void bootstrapTeacher() {
        if (accountRepository.findByLoginIdentifierAndArchivedAtIsNull(teacherEmail).isPresent()) {
            return;
        }

        UUID teacherStaffId;
        Optional<StaffView> existingStaff = staffService.findStaffByStaffNumber("STAFF-TEACHER-01");
        if (existingStaff.isPresent()) {
            teacherStaffId = existingStaff.get().id();
        } else {
            StaffView teacherStaff = staffService.createStaff(
                    "STAFF-TEACHER-01",
                    "Kofi",
                    "Annan",
                    StaffType.TEACHING,
                    null,
                    LocalDate.of(2025, 1, 1)
            );
            teacherStaffId = teacherStaff.id();
        }

        boolean generated = configuredTeacherPassword == null || configuredTeacherPassword.isBlank();
        String teacherPassword = generated ? secureTokenGenerator.generateOpaqueToken().substring(0, 16) : configuredTeacherPassword;

        Account account = new Account(
                PersonType.STAFF,
                teacherStaffId,
                teacherEmail,
                "+233240000001",
                teacherEmail,
                passwordEncoder.encode(teacherPassword)
        );
        account = accountRepository.save(account);

        assignRoleIfExists(account, "TEACHER");

        log.info("Bootstrapped Staff Teacher account ({}) successfully", teacherEmail);
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
