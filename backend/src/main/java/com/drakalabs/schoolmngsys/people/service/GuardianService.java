package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Guardian;
import com.drakalabs.schoolmngsys.people.repository.GuardianRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;

    public GuardianService(GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Audited(action = "GUARDIAN_CREATED", entityType = "Guardian")
    @Transactional
    public GuardianView createGuardian(
            String firstName, String lastName, String phone, String email, String occupation, String address) {
        Guardian guardian = new Guardian(firstName, lastName, phone, email, occupation, address);
        return GuardianView.from(guardianRepository.save(guardian));
    }

    @Audited(action = "GUARDIAN_UPDATED", entityType = "Guardian")
    @Transactional
    public GuardianView updateContact(UUID guardianId, String phone, String email, String occupation, String address) {
        Guardian guardian =
                guardianRepository.findById(guardianId).orElseThrow(() -> new NotFoundException("No such guardian: " + guardianId));
        guardian.updateContact(phone, email, occupation, address);
        return GuardianView.from(guardianRepository.save(guardian));
    }
}
