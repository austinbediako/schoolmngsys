package com.drakalabs.schoolmngsys.auth.repository;

import com.drakalabs.schoolmngsys.auth.domain.AccountRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRoleRepository extends JpaRepository<AccountRole, UUID> {

    List<AccountRole> findByAccountIdAndArchivedAtIsNull(UUID accountId);
}
