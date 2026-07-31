package com.drakalabs.schoolmngsys.communication.repository;

import com.drakalabs.schoolmngsys.communication.domain.Announcement;
import com.drakalabs.schoolmngsys.communication.domain.AnnouncementStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    List<Announcement> findByStatusAndArchivedAtIsNullOrderByPublishedAtDesc(AnnouncementStatus status);
}
