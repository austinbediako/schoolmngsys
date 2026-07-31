package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.repository.GuardianRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianQueryService {

    private final GuardianRepository guardianRepository;

    public GuardianQueryService(GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Transactional(readOnly = true)
    public Page<GuardianView> list(Pageable pageable) {
        return guardianRepository.findAll(pageable).map(GuardianView::from);
    }

    @Transactional(readOnly = true)
    public GuardianView get(UUID guardianId) {
        return guardianRepository
                .findById(guardianId)
                .map(GuardianView::from)
                .orElseThrow(() -> new NotFoundException("No such guardian: " + guardianId));
    }
}
