package com.drakalabs.schoolmngsys.communication.repository;

import com.drakalabs.schoolmngsys.communication.domain.OutboxMessage;
import com.drakalabs.schoolmngsys.communication.domain.OutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    @Query("SELECT o FROM OutboxMessage o WHERE o.status = :status AND o.nextAttemptAt <= :now AND o.archivedAt IS NULL ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingToProcess(@Param("status") OutboxStatus status, @Param("now") Instant now);

    List<OutboxMessage> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    /** Scope-filtered listing for NOTIFICATION_VIEW_OWN callers (docs/11 §3) — never expose {@link #findAll} to them. */
    Page<OutboxMessage> findByRecipientId(UUID recipientId, Pageable pageable);
}
