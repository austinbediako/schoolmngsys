package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.repository.StaffQualificationRepository;
import com.drakalabs.schoolmngsys.people.repository.StaffRepository;
import com.drakalabs.schoolmngsys.shared.web.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffQueryService {

    private final StaffRepository staffRepository;
    private final StaffQualificationRepository staffQualificationRepository;

    public StaffQueryService(StaffRepository staffRepository, StaffQualificationRepository staffQualificationRepository) {
        this.staffRepository = staffRepository;
        this.staffQualificationRepository = staffQualificationRepository;
    }

    @Transactional(readOnly = true)
    public Page<StaffView> list(Pageable pageable) {
        return staffRepository.findAll(pageable).map(StaffView::from);
    }

    @Transactional(readOnly = true)
    public StaffView get(UUID staffId) {
        return staffRepository.findById(staffId).map(StaffView::from).orElseThrow(() -> new NotFoundException("No such staff member: " + staffId));
    }

    @Transactional(readOnly = true)
    public List<StaffQualificationView> listQualifications(UUID staffId) {
        return staffQualificationRepository.findByStaffIdAndArchivedAtIsNull(staffId).stream()
                .map(StaffQualificationView::from)
                .toList();
    }
}
