package com.drakalabs.schoolmngsys.communication.repository;

import com.drakalabs.schoolmngsys.communication.domain.MessageTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, UUID> {
    Optional<MessageTemplate> findByTemplateCodeAndArchivedAtIsNull(String templateCode);
}
