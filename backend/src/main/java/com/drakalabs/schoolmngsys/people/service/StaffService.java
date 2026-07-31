package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.Staff;
import com.drakalabs.schoolmngsys.people.domain.StaffQualification;
import com.drakalabs.schoolmngsys.people.domain.StaffType;
import com.drakalabs.schoolmngsys.people.repository.StaffQualificationRepository;
import com.drakalabs.schoolmngsys.people.repository.StaffRepository;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.web.error.BusinessRuleViolationException;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BR-ST-001 (immutable staff number) / BR-ST-002 (ending employment preserves history). */
@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final StaffQualificationRepository staffQualificationRepository;

    public StaffService(StaffRepository staffRepository, StaffQualificationRepository staffQualificationRepository) {
        this.staffRepository = staffRepository;
        this.staffQualificationRepository = staffQualificationRepository;
    }

    @Transactional(readOnly = true)
    public Optional<StaffView> findStaffByStaffNumber(String staffNumber) {
        return staffRepository.findByStaffNumberAndArchivedAtIsNull(staffNumber).map(StaffView::from);
    }

    @Transactional(readOnly = true)
    public Optional<StaffView> findStaffById(UUID staffId) {
        return staffRepository.findById(staffId).map(StaffView::from);
    }

    @Audited(action = "STAFF_CREATED", entityType = "Staff")
    @Transactional
    public StaffView createStaff(
            String staffNumber,
            String firstName,
            String lastName,
            StaffType staffType,
            String gesRegistrationNumber,
            LocalDate employmentStartDate) {
        staffRepository
                .findByStaffNumberAndArchivedAtIsNull(staffNumber)
                .ifPresent(
                        existing -> {
                            throw new BusinessRuleViolationException("BR-ST-001", "Staff number already in use: " + staffNumber);
                        });

        Staff staff = new Staff(staffNumber, firstName, lastName, staffType, gesRegistrationNumber, employmentStartDate);
        return StaffView.from(staffRepository.save(staff));
    }

    @Audited(action = "STAFF_UPDATED", entityType = "Staff")
    @Transactional
    public StaffView updateBio(UUID staffId, String firstName, String lastName, String gesRegistrationNumber) {
        Staff staff = getStaff(staffId);
        staff.updateBio(firstName, lastName, gesRegistrationNumber);
        return StaffView.from(staffRepository.save(staff));
    }

    @Audited(action = "STAFF_EMPLOYMENT_ENDED", entityType = "Staff")
    @Transactional
    public StaffView endEmployment(UUID staffId, LocalDate endDate) {
        Staff staff = getStaff(staffId);
        staff.endEmployment(endDate);
        return StaffView.from(staffRepository.save(staff));
    }

    @Audited(action = "STAFF_QUALIFICATION_ADDED", entityType = "StaffQualification")
    @Transactional
    public StaffQualificationView addQualification(UUID staffId, String qualification, String institution, Integer yearObtained) {
        Staff staff = getStaff(staffId);
        return StaffQualificationView.from(
                staffQualificationRepository.save(new StaffQualification(staff, qualification, institution, yearObtained)));
    }

    private Staff getStaff(UUID staffId) {
        return staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("No such staff member: " + staffId));
    }
}
