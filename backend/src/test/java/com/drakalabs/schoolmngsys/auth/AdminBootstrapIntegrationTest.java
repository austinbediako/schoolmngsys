package com.drakalabs.schoolmngsys.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import com.drakalabs.schoolmngsys.auth.repository.AccountRoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AdminBootstrapIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountRoleRepository accountRoleRepository;

    @Test
    void bootstrapsDefaultAdminAccountOnStartup() {
        Optional<Account> adminOpt = accountRepository.findByLoginIdentifierAndArchivedAtIsNull("admin@ubs.edu.gh");
        assertThat(adminOpt).isPresent();

        Account admin = adminOpt.get();
        assertThat(admin.getLoginIdentifier()).isEqualTo("admin@ubs.edu.gh");

        List<String> roles = accountRoleRepository.findByAccountIdAndArchivedAtIsNull(admin.getId()).stream()
                .map(ar -> ar.getRole().getName())
                .toList();

        assertThat(roles).contains("SYSTEM_ADMIN", "HEAD_OF_SCHOOL");
    }
}
