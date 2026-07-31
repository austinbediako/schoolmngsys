package com.drakalabs.schoolmngsys.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.auth.service.AccountCreationResult;
import com.drakalabs.schoolmngsys.auth.service.AccountProvisioningService;
import com.drakalabs.schoolmngsys.auth.service.AuthTokens;
import com.drakalabs.schoolmngsys.auth.service.AuthenticationService;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** WP-1 test plan (docs/14 §5): "permission gate denies without permission". */
@AutoConfigureMockMvc
class PermissionGateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountProvisioningService accountProvisioningService;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void anonymousRequestToProtectedEndpointIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/roles")).andExpect(status().isUnauthorized());
    }

    @Test
    void accountWithoutTheRequiredPermissionIsForbidden() throws Exception {
        AccountCreationResult provisioned = provisionWithRole("teacher.gate.forbidden", "TEACHER");
        AuthTokens tokens = authenticationService.login(
                "teacher.gate.forbidden", provisioned.temporaryPassword(), "127.0.0.1");

        mockMvc.perform(get("/api/v1/roles").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountWithTheRequiredPermissionSucceeds() throws Exception {
        AccountCreationResult provisioned = provisionWithRole("admin.gate.allowed", "SYSTEM_ADMIN");
        AuthTokens tokens = authenticationService.login(
                "admin.gate.allowed", provisioned.temporaryPassword(), "127.0.0.1");

        mockMvc.perform(get("/api/v1/roles").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());
    }

    private AccountCreationResult provisionWithRole(String identifier, String roleName) {
        String phone =
                "+2332" + String.format("%09d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000));
        AccountCreationResult provisioned =
                accountProvisioningService.createAccount(PersonType.STAFF, UUID.randomUUID(), identifier, phone, null);
        accountProvisioningService.assignRole(provisioned.account().id(), roleName);
        return provisioned;
    }
}
