package com.drakalabs.schoolmngsys.auth.repository;

import com.drakalabs.schoolmngsys.auth.domain.LoginAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
}
