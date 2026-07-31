package com.drakalabs.schoolmngsys.communication.repository;

import com.drakalabs.schoolmngsys.communication.domain.NotificationDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    List<NotificationDelivery> findByOutboxIdOrderByAttemptedAtDesc(UUID outboxId);
    List<NotificationDelivery> findByRecipientOrderByAttemptedAtDesc(String recipient);
}
