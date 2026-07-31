package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.repository.BeceRegistrationRepository;
import com.drakalabs.schoolmngsys.progression.repository.BeceResultRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeceQueryService {

    private final BeceRegistrationRepository beceRegistrationRepository;
    private final BeceResultRepository beceResultRepository;

    public BeceQueryService(
            BeceRegistrationRepository beceRegistrationRepository,
            BeceResultRepository beceResultRepository) {
        this.beceRegistrationRepository = beceRegistrationRepository;
        this.beceResultRepository = beceResultRepository;
    }

    @Transactional(readOnly = true)
    public BeceRegistrationView getRegistration(UUID registrationId) {
        return beceRegistrationRepository.findById(registrationId)
                .map(BeceRegistrationView::from)
                .orElseThrow(() -> new NotFoundException("No such BECE registration: " + registrationId));
    }

    @Transactional(readOnly = true)
    public BeceRegistrationView getRegistrationByEnrollment(UUID enrollmentId) {
        return beceRegistrationRepository.findByEnrollmentIdAndArchivedAtIsNull(enrollmentId)
                .map(BeceRegistrationView::from)
                .orElseThrow(() -> new NotFoundException("No BECE registration found for enrollment: " + enrollmentId));
    }

    @Transactional(readOnly = true)
    public List<BeceResultView> listResults(UUID registrationId) {
        return beceResultRepository.findByBeceRegistrationIdAndArchivedAtIsNull(registrationId).stream()
                .map(BeceResultView::from)
                .toList();
    }
}
