package com.drakalabs.schoolmngsys.auth.repository;

import com.drakalabs.schoolmngsys.auth.domain.PasswordResetOtp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, UUID> {

    List<PasswordResetOtp> findByAccountIdAndConsumedAtIsNull(UUID accountId);
}
